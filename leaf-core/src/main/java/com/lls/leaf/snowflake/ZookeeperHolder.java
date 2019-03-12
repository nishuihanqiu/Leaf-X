package com.lls.leaf.snowflake;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.lls.leaf.exception.CheckLastTimeException;
import com.lls.leaf.util.PropertiesUtils;
import org.apache.commons.io.FileUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.*;

/************************************
 * ZookeeperHolder
 * @author liliangshan
 * @date 2019-03-09
 ************************************/
public class ZookeeperHolder {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperHolder.class);
    private String zkAddressNode;
    private String listenAddress;
    private int workerId;
    private static final String PREFIX_ZK_PATH = "/snowflake/" + PropertiesUtils.getProperties().getProperty("leaf_x.name");
    private static final String PROP_PATH = System.getProperty("java.io.tmpdir") + PropertiesUtils.getProperty("leaf_x.name");
    private static final String PATH_FOREVER = PREFIX_ZK_PATH + "/forever";
    private String ip;
    private String port;
    private String connectionString;
    private long lastUpdatedTime;

    public ZookeeperHolder(String ip, String port, String connectionString) {
        this.ip = ip;
        this.port = port;
        this.connectionString = connectionString;
        this.listenAddress = ip + ":" + port;
    }

    public boolean init() {
        try {
            CuratorFramework curator = this.createWithOptions(connectionString, new RetryUntilElapsed(1000, 4),
                    10000, 6000);
            curator.start();
            Stat stat = curator.checkExists().forPath(PATH_FOREVER);
            if (stat == null) {
                //不存在根节点,机器第一次启动,创建/snowflake/ip:port-000000000,并上传数据
                zkAddressNode = this.createNode(curator);
                //worker id 默认是0
                this.updateLocalWorkerID(workerId);
                //定时上报本机时间给forever节点
                this.scheduledUploadData(curator, zkAddressNode);
                return true;
            } else {
                Map<String, Integer> nodeMap = Maps.newHashMap(); //ip:port->00001
                Map<String, String> realMap = Maps.newHashMap(); //ip:port->(ipport-000001)
                //存在根节点,先检查是否有属于自己的根节点
                List<String> keys = curator.getChildren().forPath(PATH_FOREVER);
                for (String key : keys) {
                    String[] nodeKey = key.split("-");
                    realMap.put(nodeKey[0], key);
                    nodeMap.put(nodeKey[0], Integer.parseInt(nodeKey[1]));
                }
                Integer workerId = nodeMap.get(listenAddress);
                if (workerId != null) {
                    //有自己的节点,zkAddressNode=ip:port
                    zkAddressNode = PATH_FOREVER + "/" + realMap.get(listenAddress);
                    this.workerId = workerId; //启动worker时使用会使用
                    if (!checkInitTimeStamp(curator, zkAddressNode))
                        throw new CheckLastTimeException("init timestamp check error,forever node timestamp gt this node time");
                    //准备创建临时节点
                    doService(curator);
                    updateLocalWorkerID(workerId);
                    logger.info("[Old NODE]find forever node have this endpoint ip-{} port-{} workid-{} childnode and start SUCCESS", ip, port, workerId);
                } else {
                    //表示新启动的节点,创建持久节点 ,不用check时间
                    String newNode = createNode(curator);
                    zkAddressNode = newNode;
                    String[] nodeKey = newNode.split("-");
                    workerId = Integer.parseInt(nodeKey[1]);
                    doService(curator);
                    updateLocalWorkerID(workerId);
                    logger.info("[New NODE]can not find node on forever node that endpoint ip-{} port-{} workid-{},create own node on forever node and start SUCCESS ", ip, port, workerId);
                }
            }
        } catch (Exception e) {
            logger.error("Start node ERROR {}", e);
            try {
                Properties properties = new Properties();
                properties.load(new FileInputStream(new File(PROP_PATH.replace("{port}", port + ""))));
                workerId = Integer.valueOf(properties.getProperty("workerId"));
                logger.warn("START FAILED ,use local node file properties workerId-{}", workerId);
            } catch (Exception e1) {
                logger.error("Read file error ", e1);
                return false;
            }
        }

        return true;
    }

    private void doService(CuratorFramework curator) {
        scheduledUploadData(curator, zkAddressNode);// /snowflake_forever/ip:port-000000001
    }

    private void updateLocalWorkerID(int workerId) {
        File leafConfigFile = new File(PROP_PATH.replace("{port}", port));
        boolean isExists = leafConfigFile.exists();
        logger.info("leaf config file exists status is {}", isExists);
        if (isExists) {
            try {
                FileUtils.writeStringToFile(leafConfigFile, "workerId=" + workerId, false);
                logger.info("update file cache workerId is {}", workerId);
            } catch (IOException e) {
                logger.error("update file cache error ", e);
            }
        }
    }

    private void scheduledUploadData(final CuratorFramework curator, final String zkAddressNode) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r, "schedule-upload-time");
                thread.setDaemon(true);
                return thread;
            }
        });

        executorService.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                updateNewData(curator, zkAddressNode);
            }
        }, 1L, 3L, TimeUnit.SECONDS);
    }

    private boolean checkInitTimeStamp(final CuratorFramework curator, String zkAddressNode) throws Exception {
        byte[] bytes = curator.getData().forPath(zkAddressNode);
        Endpoint endPoint = deBuildData(new String(bytes));
        //该节点的时间不能小于最后一次上报的时间
        return !(endPoint.getTimestamp() > System.currentTimeMillis());
    }

    private CuratorFramework createWithOptions(String connectionString, RetryPolicy retryPolicy, int connectionTimeoutMs, int sessionTimeoutMs) {
        return CuratorFrameworkFactory.builder().connectString(connectionString)
                .sessionTimeoutMs(sessionTimeoutMs)
                .connectionTimeoutMs(connectionTimeoutMs)
                .retryPolicy(retryPolicy)
                .build();
    }

    private String createNode(CuratorFramework curator) throws Exception {
        try {
            return curator.create().creatingParentsIfNeeded().withMode(CreateMode.PERSISTENT_SEQUENTIAL)
                    .forPath(PATH_FOREVER + "/" + listenAddress + "-", buildData().getBytes());
        } catch (Exception e) {
            logger.error("create node error:{}", e.getMessage());
            throw e;
        }
    }

    private void updateNewData(CuratorFramework curator, String path) {
        if (System.currentTimeMillis() < lastUpdatedTime) {
            return;
        }
        try {
            curator.setData().forPath(path, buildData().getBytes());
            lastUpdatedTime = System.currentTimeMillis();
        } catch (Exception e) {
            logger.error("update new data error:{}, path:{}", e.getMessage(), path);
        }
    }

    /**
     * 构建需要上传的数据
     *
     * @return STR
     */
    private String buildData() throws JsonProcessingException {
        Endpoint endpoint = new Endpoint(ip, port, System.currentTimeMillis());
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(endpoint);
    }

    private Endpoint deBuildData(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        Endpoint endpoint = mapper.readValue(json, Endpoint.class);
        return endpoint;
    }

    static class Endpoint {
        private String ip;
        private String port;
        private long timestamp;

        public Endpoint() {
        }

        public Endpoint(String ip, String port, long timestamp) {
            this.ip = ip;
            this.port = port;
            this.timestamp = timestamp;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getPort() {
            return port;
        }

        public void setPort(String port) {
            this.port = port;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }

    }

    public String getZkAddressNode() {
        return zkAddressNode;
    }

    public void setZkAddressNode(String zkAddressNode) {
        this.zkAddressNode = zkAddressNode;
    }

    public String getListenAddress() {
        return listenAddress;
    }

    public void setListenAddress(String listenAddress) {
        this.listenAddress = listenAddress;
    }

    public int getWorkerId() {
        return workerId;
    }

    public void setWorkerId(int workerId) {
        this.workerId = workerId;
    }
}
