/**
 * Copyright 2015-2019 Ken Dobson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.co.magictractor.spew.core.response.parser.xpath;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import com.google.common.base.MoreObjects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import uk.co.magictractor.spew.api.SpewConnectionConfiguration;
import uk.co.magictractor.spew.api.SpewHttpMessage;
import uk.co.magictractor.spew.core.response.parser.ObjectCentricHttpMessageBodyReader;
import uk.co.magictractor.spew.util.ExceptionUtil;
import uk.co.magictractor.spew.util.HttpMessageUtil;

/**
 *
 */
public class JavaXPathHttpMessageBodyReader
        // extends AbstractSpewParsedResponse
        implements ObjectCentricHttpMessageBodyReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(JavaXPathHttpMessageBodyReader.class);

    private final XPath xpath;
    private final Document xml;

    /**
     * Default visibility because instances should only be created via
     * JaywayResponseParserInit.
     */
    JavaXPathHttpMessageBodyReader(SpewConnectionConfiguration connectionConfiguration, SpewHttpMessage httpMessage) {

        xpath = XPathFactory.newInstance().newXPath();
        //String expression = "/widgets/widget";

        //xml = new InputSource(bodyReader);

        xml = ExceptionUtil.call(() -> readXml(httpMessage));

        //NodeList nodes = (NodeList) xpath.evaluate(expression, inputSource, XPathConstants.NODESET);
    }

    private Document readXml(SpewHttpMessage httpMessage)
            throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        return builder.parse(HttpMessageUtil.createBodyInputStream(httpMessage));
    }

    @Override
    public <T> T getObject(String expr, Class<T> type) {
        return ExceptionUtil.call(() -> xpath.evaluateExpression(expr, xml, type));
        // throw ExceptionUtil.notYetImplemented();
    }

    @Override
    public <T> List<T> getList(String expr, Class<T> elementType) {
        throw ExceptionUtil.notYetImplemented();
    }

    // map key from JsonPath to XPath
    // https://goessner.net/articles/JsonPath/
    private String mapExpr(String expr) {
        String mapped;
        if (expr.startsWith("$.")) {
            mapped = "/" + expr.substring(2).replace(".", "/");
        }
        else if (!expr.contains("/")) {
            // mapped = "//rsp/@" + key; // gets stat, not err & msg
            // mapped = "/rsp/@" + key; // gets stat, not err & msg
            mapped = "/rsp/@" + expr;
        }
        // temp, while I play...
        else if (expr.startsWith("/")) {
            mapped = expr;
        }
        else {
            throw new IllegalArgumentException("Code needs modified to map key " + expr);
        }

        LOGGER.info("JsonPath mapped to XPath: {} -> {}", expr, mapped);

        return mapped;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                //.add("ctx.json", ctx.json())
                .toString();
    }

}
