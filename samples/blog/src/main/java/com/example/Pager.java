package com.example;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;

@Component
public class Pager<T> {

    @Autowired
    HttpServletRequest request;

    public String renderList(Page<T> page) throws UnsupportedEncodingException {
        return new PagerRenderer<>(page, new ServletServerHttpRequest(request)).render();
    }

    @Data
    static class PagerRenderer<T> {
        private final Page<T> page;
        private final HttpRequest request;
        private final int maxDisplayCount = 5;

        String render() throws UnsupportedEncodingException {
            StringBuilder sb = new StringBuilder();
            long current = page.getNumber();
            BeginAndEnd beginAndEnd = calcBeginAndEnd();
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpRequest(request);

            sb.append("<li");
            if (page.isFirst()) {
                sb.append(" class=\"disabled\"");
            }
            sb.append(">");
            if (!page.isFirst()) {
                builder.replaceQueryParam("page", 0);
                sb.append("<a href=\"");
                sb.append(UriUtils.decode(builder.build().toString(), "UTF-8"));
                sb.append("\">");
            }
            sb.append("&lt;&lt;");
            if (!page.isFirst()) {
                sb.append("</a>");
            }
            sb.append("</li>");
            for (long p = beginAndEnd.begin; p <= beginAndEnd.end; p++) {
                boolean active = (p == current);
                builder.replaceQueryParam("page", p);
                sb.append("<li");
                if (active) {
                    sb.append(" class=\"active\"");
                }
                sb.append(">");
                if (!active) {
                    sb.append("<a href=\"");
                    sb.append(UriUtils.decode(builder.build().toString(), "UTF-8"));
                    sb.append("\">");
                }
                sb.append(p + 1);
                if (!active) {
                    sb.append("</a>");
                }
                sb.append("</li>");
            }
            sb.append("<li");
            if (page.isLast()) {
                sb.append(" class=\"disabled\"");
            }
            sb.append(">");
            if (!page.isLast()) {
                builder.replaceQueryParam("page", page.getTotalPages() - 1);
                sb.append("<a href=\"");
                sb.append(UriUtils.decode(builder.build().toString(), "UTF-8"));
                sb.append("\">");
            }
            sb.append("&gt;&gt;");
            if (!page.isLast()) {
                sb.append("</a>");
            }
            sb.append("</li>");
            return sb.toString();
        }

        BeginAndEnd calcBeginAndEnd() {
            long begin = Math.max(0, this.page.getNumber() - (int) Math.floor(this.maxDisplayCount * 0.5));
            long end = begin + this.maxDisplayCount - 1;
            long last = this.page.getTotalPages() - 1;
            if (end > last) {
                end = last;
                begin = Math.max(0, end - (this.maxDisplayCount - 1));
            }
            return new BeginAndEnd(begin, end);
        }
    }

    @Data
    static class BeginAndEnd {
        final long begin;
        final long end;
    }
}
