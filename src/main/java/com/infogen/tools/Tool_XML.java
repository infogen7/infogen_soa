package com.infogen.tools;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 解析html
 * 
 * @author larry
 * @email larry.lv.word@gmail.com
 * @version 创建时间 2013-6-14 下午3:56:48
 */
public class Tool_XML {
	private static Logger logger = Logger.getLogger(Tool_XML.class.getName());

	private static String ARGS_SEPARATOR = " ";
	private static CommandLineParser parser = new GnuParser();
	public static final Options options = new Options();

	private static final Option option_select = new Option("select", "select", true, "根据 select 查找节点");
	private static final Option option_id = new Option("id", "id", true, "根据 id 查找节点");
	private static final Option option_tag = new Option("tag", "tag", true, "根据 tag 查找节点");
	private static final Option option_class = new Option("class", "class", true, "根据 class 查找节点");

	private static final Option option_next = new Option("next", "next", false, "根据 next 查找节点");
	private static final Option option_pre = new Option("pre", "pre", false, "根据 pre 查找节点");
	private static final Option option_nextsibling = new Option("nextsibling", "nextsibling", false, "根据 next 查找节点");
	private static final Option option_presibling = new Option("presibling", "presibling", false, "根据 pre 查找节点");
	private static final Option option_parent = new Option("parent", "parent", false, "根据 parent 查找节点");
	private static final Option option_last = new Option("last", "last", false, "根据 last 查找节点");
	private static final Option option_first = new Option("first", "first", false, "根据 first 查找节点");
	private static final Option option_index = new Option("index", "index", true, "根据 index 查找节点");

	private static final Option option_text = new Option("text", "text", false, "根据 text 查找值");
	private static final Option option_owntext = new Option("owntext", "owntext", false, "根据 text 查找值");
	private static final Option option_attr = new Option("attr", "attr", true, "根据 attr 查找值");
	private static final Option option_html = new Option("html", "html", false, "根据html 查找值");

	static {
		options.addOption(option_select);
		options.addOption(option_id);
		options.addOption(option_tag);
		options.addOption(option_class);

		options.addOption(option_next);
		options.addOption(option_pre);
		options.addOption(option_nextsibling);
		options.addOption(option_presibling);
		options.addOption(option_parent);
		options.addOption(option_last);
		options.addOption(option_first);
		options.addOption(option_index);

		options.addOption(option_text);
		options.addOption(option_owntext);
		options.addOption(option_attr);
		options.addOption(option_html);

	}

	// ///////////////////////////////////////////////////////////////公有方法/////////////////////////////////////////////////////////////////////////////
	/** 获取安全方法链的 elements */
	public static Elements elements(String attrbute, Element doc, String aql) {
		logger.debug("#获取 html 节点组 safety ".concat(aql));
		Elements elements = new Elements();
		try {
			Object tmp_result = analysis(doc, aql);
			if (tmp_result != null && tmp_result instanceof Elements) {
				elements = (Elements) tmp_result;
			}
		} catch (Exception e) {
			logger.debug("#获取所有节点 错误: ".concat(attrbute));
		}
		return elements;
	}

	/** 获取安全方法链的值，方法中出现错误,或者返回值为空时返回传入的 value */
	/**
	 * 根据jsoup 语句
	 * 
	 * @param attrbute
	 *            记录log 日志中的提示信息
	 * @param doc
	 *            要解析的文档
	 * @param aql
	 *            解析文档的 jsoup 分析语句
	 * @param default_result
	 *            如果根据 分析语句没有获得结果所返回的默认值
	 * @return
	 */
	public static String value(String attrbute, Element doc, String aql, String default_result) {
		Object tmp_result = null;
		try {
			tmp_result = analysis(doc, aql);
		} catch (Exception e) {
			logger.info("#获取 xml 节点 错误: ".concat(attrbute));
		}
		default_result = (tmp_result == null || tmp_result.toString().isEmpty()) ? default_result : tmp_result.toString().trim().replaceAll("\"", "");
		return default_result;
	}

	// ///////////////////////////////////////////////////////////////私有方法//////////////////////////////////////////////////////////////////////
	/**
	 * 解析 aql
	 * 
	 * @param doc
	 *            要解析的文档
	 * @param aql
	 *            用于解析的jsoup 语句
	 * @return 解析结果
	 * @throws Exception
	 */
	private static Object analysis(Element doc, String aql) throws Exception {
		Object result = doc;
		if (aql == null || aql.isEmpty()) {
			return null;
		}
		aql = aql.trim().substring(1);
		String[] split = aql.split(" -");
		for (String string : split) {
			String args_string = "-".concat(string);
			if (result instanceof Element) {
				result = by_element((Element) result, args_string);
			} else if (result instanceof Elements) {
				result = by_elements((Elements) result, args_string);
			} else {
				return null;
			}
		}
		return result;
	}

	/**
	 * 根据 Element 获取值
	 * 
	 * @param element
	 * @param args_string
	 * @return
	 * @throws ParseException
	 */
	private static Object by_element(Element element, String args_string) throws ParseException {
		args_string = args_string.trim();
		CommandLine parse = parser.parse(options, args_string.split(ARGS_SEPARATOR));
		if (parse.hasOption(option_tag.getOpt())) {
			return element.getElementsByTag(parse.getOptionValue(option_tag.getOpt()));
		} else if (parse.hasOption(option_class.getOpt())) {
			return element.getElementsByClass(parse.getOptionValue(option_class.getOpt()));
		} else if (parse.hasOption(option_id.getOpt())) {
			return element.getElementById(parse.getOptionValue(option_id.getOpt()));
		} else if (parse.hasOption(option_select.getOpt())) {
			return element.select(parse.getOptionValue(option_select.getOpt()));
		} else if (parse.hasOption(option_parent.getOpt())) {
			return element.parent();
		} else if (parse.hasOption(option_attr.getOpt())) {
			return element.attr(parse.getOptionValue(option_attr.getOpt()));
		} else if (parse.hasOption(option_next.getOpt())) {
			return element.nextElementSibling();
		} else if (parse.hasOption(option_pre.getOpt())) {
			return element.previousElementSibling();
		} else if (parse.hasOption(option_text.getOpt())) {
			return element.text();
		} else if (parse.hasOption(option_owntext.getOpt())) {
			return element.ownText();
		} else if (parse.hasOption(option_nextsibling.getOpt())) {
			return element.nextSibling();
		} else if (parse.hasOption(option_presibling.getOpt())) {
			return element.previousSibling().toString();
		} else if (parse.hasOption(option_html.getOpt())) {
			return element.html();
		} else {
			logger.warn("#方法不能识别该参数:".concat(args_string));
			return null;
		}
	}

	/**
	 * 根据 Elements 获取值
	 * 
	 * @param elements
	 * @param args_string
	 * @return
	 * @throws ParseException
	 */
	private static Object by_elements(Elements elements, String args_string) throws ParseException {
		args_string = args_string.trim();
		if (elements.isEmpty()) {
			return null;
		}
		CommandLine parse = parser.parse(options, args_string.split(ARGS_SEPARATOR));
		if (parse.hasOption(option_select.getOpt())) {
			return elements.select(parse.getOptionValue(option_select.getOpt()));
		} else if (parse.hasOption(option_first.getOpt())) {
			return elements.first();
		} else if (parse.hasOption(option_last.getOpt())) {
			return elements.last();
		} else if (parse.hasOption(option_index.getOpt())) {
			return elements.get(Integer.parseInt(parse.getOptionValue(option_index.getOpt())));
		} else if (parse.hasOption(option_text.getOpt())) {
			return elements.text();
		} else if (parse.hasOption(option_attr.getOpt())) {
			return elements.attr(parse.getOptionValue(option_attr.getOpt()));
		}else if (parse.hasOption(option_html.getOpt())) {
			return elements.html();
		}  else {
			logger.warn("#方法不能识别该参数:".concat(args_string));
			return null;
		}
	}

	// /////////////////////////////映射 html 页面/////////////////////////////////////
	/**
	 * 映射本地的 html 文件为 document
	 */
	public static Document create_bypath(String path_html, String encoding) throws IOException {
		File input = new File(path_html);
		Document doc = Jsoup.parse(input, encoding);
		return doc;
	}

	public static Document create(String source) {
		source = source == null ? "" : source;
		Document doc = Jsoup.parse(source);
		return doc;
	}
}