package com.server.project.parsehouse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.safari.SafariDriver;

import com.google.gson.Gson;
import com.server.project.api.Community;
import com.server.project.api.House;
import com.server.project.api.Picture;
import com.server.project.api.Point;
import com.server.project.tool.PointCreator;

public class HouseParser {
	WebDriver driver;

	public void parseAddress() throws Exception {
		System.setProperty("webdriver.chrome.driver", "/home/soslab/Desktop/SoslabProjectHouseParser/chromedriver");
		driver = new ChromeDriver();
		// driver = new SafariDriver();
		try {
			// parse 信義房屋
			// navigate to house list
			int endNum = 0;
			Class.forName("org.postgresql.Driver").newInstance();
			String url = "jdbc:postgresql://140.119.19.33:5432/project";
			Connection con = DriverManager.getConnection(url, "postgres", "093622"); // 帳號密碼
			Statement selectST = con.createStatement();

			String getHouseNumSQL = "select count('id') from house;";
			ResultSet selectRS = selectST.executeQuery(getHouseNumSQL);
			while (selectRS.next()) {
				endNum = Integer.valueOf(selectRS.getString(1));
			}

			int startPage = (endNum / 30) + 1;
			int startNum = (endNum % 30) + 1;
			driver.get("http://buy.sinyi.com.tw/list/" + (startPage - 1) + ".html");

			System.out.println("start from page" + startPage);
			List<WebElement> button = driver.findElement(By.id("search_pagination")).findElements(By.tagName("li"));
			boolean breakIt = true;
			while (true) {
				breakIt = true;
				try {
					for (WebElement li : button) {
						String text = li.getText();
						if (text.equals(String.valueOf(startPage))) {
							System.out.println("click");
							li.click();
							Thread.sleep(4000);
							break;
						}
					}
				} catch (Exception e) {
					if (e.getMessage().contains("element is not attached")) {
						breakIt = false;
						System.out.println("try again");
					}
				}
				if (breakIt) {
					break;
				}

			}
			// navigate to each page
			for (int i = startNum - 1; i < 30; i++) {
				System.out.println("start parse No." + (i + 1));
				House house = seleniumParse(i);
				System.out.println("start insert to database");
				Gson gson = new Gson();
				System.out.println(gson.toJson(house));
				locationInsertIntoDB(house);
			}

			for (int j = startPage + 1; j < 100; j++) {
				driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
				System.out.println("start from page" + j);
				button = driver.findElement(By.id("search_pagination")).findElements(By.tagName("li"));
				breakIt = true;
				while (true) {
					breakIt = true;
					try {
						for (WebElement li : button) {
							String text = li.getText();
							if (text.equals(String.valueOf(j))) {
								System.out.println("click");
								li.click();
								Thread.sleep(4000);
								break;
							}
						}
					} catch (Exception e) {
						if (e.getMessage().contains("element is not attached")) {
							breakIt = false;
							System.out.println("try again");
						}
					}
					if (breakIt) {
						break;
					}

				}
				// navigate to each page
				for (int i = 0; i < 30; i++) {
					System.out.println("start parse No." + (i + 1));
					House house = seleniumParse(i);
					System.out.println("start insert to database");
					Gson gson = new Gson();
					System.out.println(gson.toJson(house));
					locationInsertIntoDB(house);
				}
			}
			con.close();
			selectRS.close();
			selectST.close();
			driver.close();
			// driver.quit();
			System.out.println("complete parse the web and start to inert into DB.");
		} catch (IndexOutOfBoundsException e) {
			driver.close();
			// driver.quit();
			parseAddress();
		}
	}

	// 把爬到的經緯度資料存入資料庫
	public void locationInsertIntoDB(House house) throws ClassNotFoundException, SQLException, IOException {
		PointCreator pointCreator = new PointCreator();
		Point addressPoint = pointCreator.createPointByRoad(house.getAddress());
		Connection c = null;
		// connect DB
		Class.forName("org.postgresql.Driver");
		c = DriverManager.getConnection("jdbc:postgresql://140.119.19.33:5432/project", "postgres", "093622");
		// insert each location into table
		Statement stmt = c.createStatement();
		String insertHouseSQL = "INSERT INTO house(title, description, location, price, address, type, url, address_point, picture, community, life, information, square) VALUES('"
				+ house.getTitle() + "', '" + house.getDescription() + "', ST_GeomFromText('POINT("
				+ house.getLocation() + ")', 4326), '" + house.getPrice() + "', '" + house.getAddress() + "', '"
				+ house.getType() + "', '" + house.getUrl() + "', ST_GeomFromText('POINT(" + addressPoint.getLng() + " "
				+ addressPoint.getLat() + ")', 4326), '" + house.getPicture() + "', '" + house.getCommunity() + "', '"
				+ house.getLife() + "', '" + house.getInformation() + "', '" + house.getSquare() + "');";
		System.out.println(insertHouseSQL);
		stmt.executeUpdate(insertHouseSQL);

		stmt.close();
		c.close();
		System.out.println("insert");
	}

	private House seleniumParse(int index) {
		Gson gson = new Gson();
		List<WebElement> resultItems = driver.findElements(By.className("search_result_item"));
		House house = new House();
		WebElement item = resultItems.get(index);
		String itemURL = item.findElement(By.tagName("a")).getAttribute("href");
		// WebDriver itemDriver = new SafariDriver();
		WebDriver itemDriver = new ChromeDriver();
		itemDriver.get(itemURL);

		// get location URL
		WebElement itemMap = itemDriver.findElement(By.id("static_map"));
		String itemlocationURL = itemMap.getAttribute("data-src");
		// get 經緯度
		String[] splitURl = itemlocationURL.replace(".png", "").split("_");
		// 緯度
		String latitude = splitURl[splitURl.length - 2];
		// 經度
		String longitude = splitURl[splitURl.length - 1];
		String itemLocation = longitude + " " + latitude;
		house.setLocation(itemLocation);

		// get title
		String itemTitleElement = itemDriver.findElement(By.id("content-main")).findElement(By.tagName("h2")).getText();
		int titleIndex = itemTitleElement.indexOf("(");
		String itemTitle = itemTitleElement.substring(0, titleIndex);
		house.setTitle(itemTitle);

		// get price
		String itemPrice = itemDriver.findElement(By.id("obj-info")).findElement(By.className("price")).getText() + "萬";
		house.setPrice(itemPrice);

		// get address
		String itemAddress = itemDriver.findElement(By.id("content-main")).findElement(By.tagName("h1")).getText();
		house.setAddress(itemAddress);

		// get type
		String itemType = itemDriver.findElement(By.id("obj-info")).findElements(By.tagName("li")).get(3).getText();
		house.setType(itemType);

		// get url
		String itemUrl = itemDriver.getCurrentUrl();
		house.setUrl(itemUrl);

		// get house info
		StringBuilder houseInfoBuilder = new StringBuilder();
		List<WebElement> infoTable = itemDriver.findElement(By.id("basic-data")).findElement(By.tagName("table"))
				.findElements(By.tagName("tr"));
		for (int i = 1; i < infoTable.size(); i++) {
			if (i == 5) {
				List<WebElement> infoList = infoTable.get(i).findElements(By.tagName("td"));
				StringBuilder partString = new StringBuilder();
				for (WebElement info : infoList) {
					partString.append(info.getText()).append("@");
				}
				String partContent = partString.toString().replaceAll(" ", ": ").replaceAll("\n", "@ ");
				partContent = partContent + " ";
				houseInfoBuilder.append(partContent);
			} else {
				List<WebElement> infoList = infoTable.get(i).findElements(By.tagName("td"));
				int count = 0;
				for (WebElement info : infoList) {
					houseInfoBuilder.append(info.getText());
					count++;
					if (count % 2 == 0) {
						houseInfoBuilder.append("@ ");
					} else {
						houseInfoBuilder.append(": ");
					}
				}
			}

		}
		houseInfoBuilder.delete(houseInfoBuilder.length() - 2, houseInfoBuilder.length());
		String houseInfo = houseInfoBuilder.toString().replaceAll("\n", ", ").replace("貸款試算", "");
		house.setInformation(houseInfo);

		// get community
		boolean exists = itemDriver.findElements(By.id("belong_com_info")).size() != 0;
		if (exists) {
			Community community = new Community();
			String comTitle = itemDriver.findElement(By.id("belong_com_info")).findElement(By.tagName("h3")).getText();
			community.setName(comTitle);
			List<WebElement> comDescriptions = itemDriver.findElement(By.id("belong_com_info"))
					.findElements(By.tagName("p"));
			String comDescription = comDescriptions.get(1).getText();
			community.setDescription(comDescription);
			house.setCommunity(gson.toJson(community));
		}

		// get life info
		StringBuilder life = new StringBuilder();
		List<WebElement> lifeTable = itemDriver.findElement(By.id("life-info")).findElement(By.tagName("table"))
				.findElements(By.tagName("tr"));
		if (lifeTable.size() != 0) {
			String checkMRT = lifeTable.get(lifeTable.size() - 1).getText();
			if (checkMRT.contains("捷運")) {
				if (lifeTable.size() != 1) {
					for (int i = 0; i < lifeTable.size() - 1; i++) {
						List<WebElement> lifeDetails = lifeTable.get(i).findElements(By.tagName("td"));
						int count = 0;
						for (WebElement lifeDetail : lifeDetails) {
							life.append(lifeDetail.getText());
							count++;
							if (count % 2 == 0) {
								life.append(", ");
							} else {
								life.append(": ");
							}
						}
					}
				}
			} else {
				for (int i = 0; i < lifeTable.size(); i++) {
					List<WebElement> lifeDetails = lifeTable.get(i).findElements(By.tagName("td"));
					int count = 0;
					for (WebElement lifeDetail : lifeDetails) {
						life.append(lifeDetail.getText());
						count++;
						if (count % 2 == 0) {
							life.append(", ");
						} else {
							life.append(": ");
						}
					}
				}
			}
			if (life.length() > 2) {
				life.delete(life.length() - 2, life.length());
			}
			house.setLife(life.toString());
			System.out.println(life);
		}

		// get picture
		Picture picture = new Picture();
		List<String> itemPictureList = new ArrayList<String>();
		List<WebElement> itemPictureelements = itemDriver.findElement(By.id("photo_list_layout"))
				.findElements(By.tagName("img"));
		if (itemPictureelements.size() > 5) {
			for (int webEleIndex = 0; webEleIndex < 5; webEleIndex++) {
				String itemPicture = itemPictureelements.get(webEleIndex).getAttribute("src");
				if (itemPicture.isEmpty()) {
				} else {
					itemPicture = itemPicture.replace("thumb", "album");
					itemPictureList.add(itemPicture);
				}
			}
		} else {
			for (int webEleIndex = 0; webEleIndex < itemPictureelements.size(); webEleIndex++) {
				String itemPicture = itemPictureelements.get(webEleIndex).getAttribute("src");
				if (itemPicture.isEmpty()) {
				} else {
					itemPicture = itemPicture.replace("thumb", "album");
					itemPictureList.add(itemPicture);
				}
			}
		}
		picture.setPictureURL(itemPictureList);
		house.setPicture(itemPictureList.toString());

		// get square
		String itemSquare = itemDriver.findElement(By.id("obj-info")).findElements(By.tagName("li")).get(1).getText();
		house.setSquare(itemSquare);

		String itemPattern = itemDriver.findElement(By.id("obj-info")).findElements(By.tagName("li")).get(4).getText();
		if (itemPattern.contains("社區")) {
			// get description
			String itemDescription = itemDriver.findElement(By.id("obj-info")).findElements(By.tagName("li")).get(7)
					.getText();
			itemDescription = itemDescription.replaceAll("\n", ",");
			house.setDescription(itemDescription);
		} else {
			// get description
			String itemDescription = itemDriver.findElement(By.id("obj-info")).findElements(By.tagName("li")).get(6)
					.getText();
			itemDescription = itemDescription.replaceAll("\n", ", ");
			house.setDescription(itemDescription);
		}
		itemDriver.quit();
		// itemDriver.close();
		return house;
	}
}
