package uk.co.magictractor.oauth.processor.flickr;

import uk.co.magictractor.oauth.common.Tag;
import uk.co.magictractor.oauth.common.TagSet;
import uk.co.magictractor.oauth.common.TagType;
import uk.co.magictractor.oauth.flickr.pojo.FlickrPhoto;
import uk.co.magictractor.oauth.processor.Processor;

/**
 * Check that tags are known, and that there is a terminal tag (such as "red admiral" rather than just "butterfly").
 */
public class TagCheckProcessor implements Processor<FlickrPhoto, MutablePhoto, FlickrProcessorContext> {

	@Override
	public void process(MutablePhoto photo, FlickrProcessorContext context) {
		TagSet tags = photo.getTagSet();

		checkTagType(TagType.SUBJECT, tags, context);
		checkTagType(TagType.LOCATION, tags, context);
		checkNoUnknownTags(tags, context);
	}

	private void checkTagType(TagType tagType, TagSet tags, FlickrProcessorContext context) {
		Tag deepestTag = tags.getDeepestTag(tagType);
		if (deepestTag == null) {
			System.err.println("No tag of type " + tagType);
		} else if (deepestTag.hasChildren()) {
			System.err.println("Deepest tag of type " + tagType + " has more specific child tags: " + deepestTag);
		}
	}

	private void checkNoUnknownTags(TagSet tags, FlickrProcessorContext context) {
		for (Tag tag : tags.getTags()) {
			if (tag.isUnknown()) {
				context.addUnknownTag(tag);
			}
		}
	}
}