package com.rs.game.player.content.custom;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.rs.game.World;
import com.rs.game.task.Task;



/**
 * @author Robbie
 * Provides real-time news updates to players
 */
public class NewsUpdate extends Task {

	public NewsUpdate() {
		super(200, true);
	}

	private int count = 0;
	private String[] newsHeadlines;

	@Override
	protected void execute() {
		if (count == 0)
			newsHeadlines = getNewsHeadlines();
		else
			World.sendWorldMessage("@blu@Newsflash:@bla@ " + getCurrentHeadline(),false);
		count++;
		if (count == 10)
			count = 0;
	}

	public String getCurrentHeadline() {
		return newsHeadlines[count - 1];
	}

	private String getNewsData() {
		String line = null;
		try {
			URL url = new URL(
					"http://news.google.com/news?pz=1&cf=all&ned=us&hl=en&topic=w&output=rss");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.addRequestProperty("User-Agent", "Mozilla/4.76");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					con.getInputStream()));
			line = in.readLine();
			in.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return line;
	}

	private String[] getNewsHeadlines() {
		String p = getNewsData();
		String[] data = null;
		if (p != null) {
			data = p.split(">");
			for (int i = 0; i < data.length; i++) {
				data[i] = data[i].replaceAll("<", "");
				data[i] = data[i].replaceAll(">", "");
				data[i] = data[i].replaceAll("/title", "");
				data[i] = data[i].replaceAll("&apos;", "'");
			}
			String[] headlines = { data[28], data[42], data[56], data[70],
					data[84], data[98], data[112], data[126], data[140],
					data[154] };
			return headlines;
		}
		return null;
	}

	

}