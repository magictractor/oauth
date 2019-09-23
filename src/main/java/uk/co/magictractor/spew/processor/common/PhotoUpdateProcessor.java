package uk.co.magictractor.spew.processor.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.magictractor.spew.photo.Image;
import uk.co.magictractor.spew.processor.Processor;

public abstract class PhotoUpdateProcessor implements Processor<Image, MutablePhoto, PhotoProcessorContext> {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    protected Logger getLogger() {
        return logger;
    }

}
