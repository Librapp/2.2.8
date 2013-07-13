package com.sumavision.talktv2.data;

public class ProgramPlayPosition {
	public int id;
	public int type;
	public int playPosition;
	public int fenji;

	public ProgramPlayPosition(int id, int type, int playPosition,
			int fenji) {
		super();
		this.id = id;
		this.type = type;
		this.playPosition = playPosition;
		this.fenji = fenji;
	}

	public ProgramPlayPosition() {

	}
}
