package org.jbake.app;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.configuration.CompositeConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class RendererTest {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	
	private File sourceFolder;
	private File destinationFolder;
	private File templateFolder;
	private CompositeConfiguration config;
		
	@Before
	public void setup() throws Exception, IOException, URISyntaxException {
//		URL cwd = this.getClass().getResource("default.properties");
//		URL refFileUrl = ClassLoader.getSystemResource("misc");
//		System.out.println(refFileUrl);
//		File refFile = new File(refFileUrl.toURI());
//		source = refFile.getParentFile();
//		System.out.println(source);
//		String source = System.getProperty("user.dir") + File.separator + "misc";
		URL sourceUrl = this.getClass().getResource("/");
		
		sourceFolder = new File(sourceUrl.getFile());
		if (!sourceFolder.exists()) {
			throw new Exception("Cannot find sample data structure!");
		}
		
		destinationFolder = folder.getRoot();
		
		templateFolder = new File(sourceFolder, "templates");
		if (!templateFolder.exists()) {
			throw new Exception("Cannot find template folder!");
		}

		config = new CompositeConfiguration();
		config.addConfiguration(new PropertiesConfiguration("default.properties"));
		Assert.assertNotNull(config.getString("template.folder"));
	}
	
	@Test
	public void render() throws Exception {
		Parser parser = new Parser();
		Renderer renderer = new Renderer(sourceFolder, destinationFolder, templateFolder, config);
		
		File sampleFile = new File(sourceFolder.getPath()+File.separator+"content"+File.separator+"blog"+File.separator+"2013"+File.separator+"second-post.html");
		Map<String, Object> content = parser.processFile(sampleFile);
		content.put("uri", "/second-post.html");
		renderer.render(content);
		File outputFile = new File(destinationFolder, "second-post.html");
		Assert.assertTrue(outputFile.exists());
		Scanner scanner = new Scanner(outputFile);
		
		boolean foundTitle = false;
		boolean foundDate = false;
		boolean foundBody = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("<h2>Second Post</h2>")) {
				foundTitle = true;
			}
			if (line.contains("<p class=\"post-date\">28 February 2013</p>")) {
				foundDate = true;
			}
			if (line.contains("Lorem ipsum dolor sit amet")) {
				foundBody = true;
			}
			if (foundTitle && foundDate && foundBody) {
				break;
			}
		}
		
		Assert.assertTrue(foundTitle);
		Assert.assertTrue(foundDate);
		Assert.assertTrue(foundBody);
	}
	
	@Test
	public void renderIndex() throws Exception {
		Crawler crawler = new Crawler(sourceFolder, config);
		crawler.crawl(new File(sourceFolder.getPath()+File.separator+"content"));
		Renderer renderer = new Renderer(sourceFolder, destinationFolder, templateFolder, config, crawler.getPosts(), crawler.getPages());
		renderer.renderIndex(crawler.getPosts(), "index.html");
		File outputFile = new File(destinationFolder, "index.html");
		Assert.assertTrue(outputFile.exists());
		Scanner scanner = new Scanner(outputFile);
		
		boolean foundFirstTitle = false;
		boolean foundSecondTitle = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("<h4><a href=\"/blog/2012/first-post.html\">First Post</a></h4>")) {
				foundFirstTitle = true;
			}
			if (line.contains("<h4><a href=\"/blog/2013/second-post.html\">Second Post</a></h4>")) {
				foundSecondTitle = true;
			}
			if (foundFirstTitle && foundSecondTitle) {
				break;
			}
		}
		
		Assert.assertTrue(foundFirstTitle);
		Assert.assertTrue(foundSecondTitle);
	}
	
	@Test
	public void renderFeed() throws Exception {
		Crawler crawler = new Crawler(sourceFolder, config);
		crawler.crawl(new File(sourceFolder.getPath()+File.separator+"content"));
		Renderer renderer = new Renderer(sourceFolder, destinationFolder, templateFolder, config, crawler.getPosts(), crawler.getPages());
		renderer.renderFeed(crawler.getPosts(), "feed.xml");
		File outputFile = new File(destinationFolder, "feed.xml");
		Assert.assertTrue(outputFile.exists());
		Scanner scanner = new Scanner(outputFile);
		
		boolean foundDescription = false;
		boolean foundFirstTitle = false;
		boolean foundSecondTitle = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("<description>My corner of the Internet</description>")) {
				foundDescription = true;
			}
			if (line.contains("<title>Second Post</title>")) {
				foundFirstTitle = true;
			}
			if (line.contains("<title>First Post</title>")) {
				foundSecondTitle = true;
			}
			if (foundDescription && foundFirstTitle && foundSecondTitle) {
				break;
			}
		}
		
		Assert.assertTrue(foundDescription);
		Assert.assertTrue(foundFirstTitle);
		Assert.assertTrue(foundSecondTitle);
	}
	
	@Test
	public void renderArchive() throws Exception {
		Crawler crawler = new Crawler(sourceFolder, config);
		crawler.crawl(new File(sourceFolder.getPath()+File.separator+"content"));
		Renderer renderer = new Renderer(sourceFolder, destinationFolder, templateFolder, config, crawler.getPosts(), crawler.getPages());
		renderer.renderArchive(crawler.getPosts(), "archive.html");
		File outputFile = new File(destinationFolder, "archive.html");
		Assert.assertTrue(outputFile.exists());
		Scanner scanner = new Scanner(outputFile);
		
		boolean foundFirstPost = false;
		boolean foundSecondPost = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("<h4>28 February - <a href=\"/blog/2013/second-post.html\">Second Post</a></h4>")) {
				foundFirstPost = true;
			}
			if (line.contains("<h4>27 February - <a href=\"/blog/2012/first-post.html\">First Post</a></h4>")) {
				foundSecondPost = true;
			}
			if (foundFirstPost && foundSecondPost) {
				break;
			}
		}
		
		Assert.assertTrue(foundFirstPost);
		Assert.assertTrue(foundSecondPost);
	}
	
	@Test
	public void renderTags() throws Exception {
		Crawler crawler = new Crawler(sourceFolder, config);
		crawler.crawl(new File(sourceFolder.getPath()+File.separator+"content"));
		Renderer renderer = new Renderer(sourceFolder, destinationFolder, templateFolder, config, crawler.getPosts(), crawler.getPages());
		renderer.renderTags(crawler.getPostsByTags(), "tags");
		File outputFile = new File(destinationFolder + File.separator + "tags" + File.separator + "blog.html");
		Assert.assertTrue(outputFile.exists());
		Scanner scanner = new Scanner(outputFile);
		
		boolean foundFirstPost = false;
		boolean foundSecondPost = false;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			if (line.contains("<h4>28 February - <a href=\"/blog/2013/second-post.html\">Second Post</a></h4>")) {
				foundFirstPost = true;
			}
			if (line.contains("<h4>27 February - <a href=\"/blog/2012/first-post.html\">First Post</a></h4>")) {
				foundSecondPost = true;
			}
			if (foundFirstPost && foundSecondPost) {
				break;
			}
		}
		
		Assert.assertTrue(foundFirstPost);
		Assert.assertTrue(foundSecondPost);
	}
}
