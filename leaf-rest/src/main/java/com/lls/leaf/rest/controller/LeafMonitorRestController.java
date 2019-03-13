package com.lls.leaf.rest.controller;

import com.lls.leaf.model.LeafAlloc;
import com.lls.leaf.model.SegmentBuffer;
import com.lls.leaf.rest.model.SegmentBufferView;
import com.lls.leaf.rest.service.SegmentIdService;
import com.lls.leaf.segment.SegmentIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/************************************
 * LeafMonitorRestController
 * @author liliangshan
 * @date 2019-03-13
 ************************************/
@Controller
public class LeafMonitorRestController {

    private static final Logger logger = LoggerFactory.getLogger(LeafMonitorRestController.class);

    @Autowired
    private SegmentIdService segmentIdService;

    @RequestMapping(value = "/cache")
    public String getCache(Model model) {
        Map<String, SegmentBufferView> data = new HashMap<>();
        SegmentIdGenerator segmentIdGenerator = segmentIdService.getSegmentIdGenerator();
        if (segmentIdGenerator == null) {
            throw new IllegalArgumentException("You should config leaf.segment.enable=true first");
        }

        Map<String, SegmentBuffer> cache = segmentIdGenerator.getCache();
        for (Map.Entry<String, SegmentBuffer> entry : cache.entrySet()) {
            SegmentBufferView sv = new SegmentBufferView();
            SegmentBuffer buffer = entry.getValue();
            sv.setInitOk(buffer.isInitOk());
            sv.setKey(buffer.getKey());
            sv.setPos(buffer.getCurrentIndex());
            sv.setNextReady(buffer.isNextReady());
            sv.setMax0(buffer.getSegments()[0].getMax());
            sv.setValue0(buffer.getSegments()[0].getValue().get());
            sv.setStep0(buffer.getSegments()[0].getStep());

            sv.setMax1(buffer.getSegments()[1].getMax());
            sv.setValue1(buffer.getSegments()[1].getValue().get());
            sv.setStep1(buffer.getSegments()[1].getStep());

            data.put(entry.getKey(), sv);

        }
        logger.info("Cache info {}", data);
        model.addAttribute("data", data);
        return "segment";
    }

    @RequestMapping(value = "db")
    public String getDb(Model model) {
        SegmentIdGenerator segmentIdGenerator = segmentIdService.getSegmentIdGenerator();
        if (segmentIdGenerator == null) {
            throw new IllegalArgumentException("You should config leaf.segment.enable=true first");
        }
        List<LeafAlloc> items = segmentIdGenerator.getAllLeafAllocs();
        logger.info("DB info {}", items);
        model.addAttribute("items", items);
        return "db";
    }

}
