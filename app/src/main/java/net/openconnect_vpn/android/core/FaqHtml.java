package net.openconnect_vpn.android.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.text.TextUtils;
import net.openconnect_vpn.android.R;

public class FaqHtml {

	private static String htmlEncode(String in) {
		in = TextUtils.htmlEncode(in).replace("\n", "<br>");

		StringBuilder out = new StringBuilder();
		Matcher m;

		// match markdown-formatted code: `code`
		// replace with: <tt>code</tt>
		Pattern p = Pattern.compile("`(.+?)`");
		while (true) {
			m = p.matcher(in);
			if (!m.find()) {
				break;
			}
			out.append(in.substring(0, m.start()));
			out.append("<tt>");
			out.append(m.group(1));
			out.append("</tt>");
			in = in.substring(m.end());
		}

		// match markdown-formatted links: [link text](http://foo.bar.com)
		// replace with: <a href="http://foo.bar.com">link text</a>
		p = Pattern.compile("\\[(.+?)\\]\\((\\S+)\\)");
		while (true) {
			m = p.matcher(in);
			if (!m.find()) {
				break;
			}
			out.append(in.substring(0, m.start()));
			out.append("<a href=\"" + m.group(2) + "\">");
			out.append(m.group(1));
			out.append("</a>");
			in = in.substring(m.end());
		}

		out.append(in);
		return out.toString();
	}

	/** The FAQ entries as HTML, without a document wrapper. */
	public static String build(Context ctx) {
		String items[] = ctx.getResources().getStringArray(R.array.faq_text);
		StringBuilder html = new StringBuilder();

		// "Q: " and "A: "
		String q_abbrev = ctx.getString(R.string.question_abbrev) + " ";
		String a_abbrev = ctx.getString(R.string.answer_abbrev) + " ";

		for (int i = 0; i < items.length; i += 2) {
			html.append("<b>" + q_abbrev + htmlEncode(items[i]) + "</b><br><br>");
			html.append(a_abbrev + htmlEncode(items[i + 1]) + "<br><br>");
		}
		return html.toString();
	}
}
