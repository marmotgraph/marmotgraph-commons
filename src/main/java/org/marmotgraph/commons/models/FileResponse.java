package org.marmotgraph.commons.models;

import java.util.List;
import java.util.Map;

public record FileResponse(byte[] bytes, Map<String, List<String>> headers) {

}
