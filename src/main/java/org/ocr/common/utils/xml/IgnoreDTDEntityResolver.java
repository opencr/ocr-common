package org.ocr.common.utils.xml;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * 经过检测每次加载3KB文件的xml耗时1秒钟。性能极慢。经过很久终于找到原因，原来是验证xmlDTD文件导致的。
 */
public class IgnoreDTDEntityResolver implements EntityResolver {

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {

        return new InputSource(new ByteArrayInputStream("<?xml version='1.0' encoding='UTF-8'?>".getBytes()));
    }

}
