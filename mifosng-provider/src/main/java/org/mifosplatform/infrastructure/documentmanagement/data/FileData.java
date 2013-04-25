package org.mifosplatform.infrastructure.documentmanagement.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class FileData {
    private static final Logger logger = LoggerFactory.getLogger(FileData.class);

    private final File file;
    private final String fileName;
    private final String contentType;
    private final InputStream inputStream;

    public FileData(File file, String fileName, String contentType) {
        this.file = file;
        this.fileName = fileName;
        this.contentType = contentType;
        this.inputStream = null;
    }

    public FileData(InputStream inputStream, String fileName, String contentType) {
        this.file = null;
        this.inputStream = inputStream;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public String contentType() {
        return contentType;
    }

    public String name() {
        return fileName;
    }

    public InputStream file(){
        try{
            if(inputStream == null){
                return new FileInputStream(file);
            }
            else {
                return inputStream;
            }

        }   catch (FileNotFoundException e){
            logger.error(e.toString());
            return null;
        }
    }
}

