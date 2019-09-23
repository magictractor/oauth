package uk.co.magictractor.spew.processor.common;

import uk.co.magictractor.spew.photo.Image;
import uk.co.magictractor.spew.processor.ProcessorChain;

/** Upload new photos in the local collection to a service provider. */
public class PhotoUploadProcessorChain extends ProcessorChain<Image, MutablePhoto, PhotoProcessorContext> {

    public PhotoUploadProcessorChain(PhotoUploadProcessor uploadProcessor) {

        addProcessor(uploadProcessor);
    }

}
