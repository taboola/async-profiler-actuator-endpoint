package com.taboola.async_profiler.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class NetUtils {

    public URL buildUrl(String string, String path, Map<String, String> queryParams) {
        try {
            URI baseUri = URI.create(string);
            StringBuilder queryStringBuilder = new StringBuilder();
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) {
                    queryStringBuilder.append("&");
                }

                queryStringBuilder.append(urlEncodedKeyValue(entry.getKey(), entry.getValue()));

                first = false;
            }

            return new URI(baseUri.getScheme(),
                    baseUri.getUserInfo(),
                    baseUri.getHost(),
                    baseUri.getPort(),
                    baseUri.getPath() + path,
                    queryStringBuilder.toString(),
                    null).toURL();

        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public String urlEncoded(String s) {
        try {
            return URLEncoder.encode(s, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String urlEncodedKeyValue(String key, String value) {
        return urlEncoded(key) + "=" + urlEncoded(value);
    }
}
