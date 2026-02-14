package com.zain.bh.alm.acceptance.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.zain.bh.alm.acceptance.service.BahrainPoService;

@RestController
public class BahrainController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BahrainController.class);

	private final BahrainPoService bahrainPoService;

	public BahrainController(BahrainPoService bahrainPoService) {
		this.bahrainPoService = bahrainPoService;
	}

	// Creates or updates purchase order for Bahrain
	@PostMapping(value = "/createPo")
	@CrossOrigin(origins = "*", allowedHeaders = "*", maxAge = 3600)
	public Map<String, String> createPo(@RequestBody String request) {
		LOGGER.info("PO CREATE REQUEST | {}", request);
		return bahrainPoService.createOrUpdatePo(request);
	}
}
