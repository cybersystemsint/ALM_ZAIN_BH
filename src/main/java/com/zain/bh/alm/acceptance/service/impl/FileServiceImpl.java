package com.zain.bh.alm.acceptance.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.zain.bh.alm.acceptance.entity.FileRecord;
import com.zain.bh.alm.acceptance.repository.FileRecordRepository;
import com.zain.bh.alm.acceptance.service.FileService;

@Service
public class FileServiceImpl implements FileService {

    private static final Logger LOGGER = LogManager.getLogger(FileServiceImpl.class);

    private final FileRecordRepository fileRepo;

    public FileServiceImpl(FileRecordRepository fileRepo) {
        this.fileRepo = fileRepo;
    }

    @Override
    public List<Map<String, String>> getAttachments(String poNumber, Integer dccId) {
        LOGGER.debug("Fetching attachments for PO: {}, DCC: {}", poNumber, dccId);
        List<FileRecord> fileRecords = fileRepo.findByPoNumberAndDccId(poNumber, dccId);
        
        if (fileRecords.isEmpty()) {
            throw new IllegalArgumentException("No files found for poNumber: " + poNumber + ", dccId: " + dccId);
        }
        
        return fileRecords.stream().map(file -> {
            Map<String, String> fileInfo = new HashMap<>();
            fileInfo.put("fileName", file.getFileName());
            fileInfo.put("filePath", "/files/" + file.getFileName());
            return fileInfo;
        }).collect(Collectors.toList());
    }
}
