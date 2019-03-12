package uk.co.magictractor.oauth.imgur.pojo;

import java.time.Instant;
import java.time.LocalDateTime;

import uk.co.magictractor.oauth.common.Photo;
import uk.co.magictractor.oauth.common.TagSet;

public class ImgurImage implements Photo {

	private String id;
	// Imgur uses this identifier (rather than id) when deleting images.
	private String deleteHash;
	private String title;
	private String description;
	// TODO! map from array in JSON
	// private String tags;
	private int width;
	private int height;

	@Override
	public String getServiceProviderId() {
		return id;
	}

	// TODO! use "name", which is the original file name, but with extension
	// stripped
	@Override
	public String getFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public TagSet getTagSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instant getDateTimeTaken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Instant getDateTimeUpload() {
		// TODO Auto-generated method stub
		return null;
	}

}