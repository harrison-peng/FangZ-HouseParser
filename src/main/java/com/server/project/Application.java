package com.server.project;

import com.server.project.parseserver.HouseParser;

public class Application {
	public static void main(String[] args) throws Exception {
		HouseParser houseParser = new HouseParser();
		houseParser.parseAddress();
	}
}
