package com.amtware.toyjms.consumer.flatinfo;

import java.util.Optional;

public interface ExtraInfoExtractor {

	boolean canExtractFrom(String link);
	Optional<ExtraInfo> extractExtraInfo(String link);

	public class ExtraInfo {
		private final String header;
		private final String image;
		private final String desc;
		public ExtraInfo(String header, String image, String desc) {
			this.header = header;
			this.image = image;
			this.desc = desc;
		}
		public String getHeader() {
			return header;
		}
		public String getImage() {
			return image;
		}
		public String getDesc() {
			return desc;
		}
	}

}
