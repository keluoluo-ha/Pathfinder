package com.hhk.pathfinderbacked.ai.tools;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

public class WebScrapingTool {

    @Tool(description = "Scrape the content of a web page")
    public String scrapeWebPage(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document doc = Jsoup.connect(url).timeout(10000).userAgent("Mozilla/5.0").get();
            String text = doc.body().text();
            if (text.length() > 8000) {
                return text.substring(0, 8000) + "\n...[内容已截断]";
            }
            return text;
        } catch (IOException e) {
            return "Error scraping web page: " + e.getMessage();
        }
    }
}
