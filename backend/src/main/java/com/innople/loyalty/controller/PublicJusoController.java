package com.innople.loyalty.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Juso 주소 API 콜백 수신.
 * 팝업 방식 대신 백엔드에서 수신하여 postMessage로 부모 창에 전달하는 HTML을 반환.
 * HTTPS/HTTP 혼용 시 Chrome 경고를 피하고, GET/POST 모두 처리.
 */
@RestController
@RequestMapping("/api/v1/public/juso-callback")
public class PublicJusoController {

    @GetMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String callbackGet(
            @RequestParam(required = false) String zipNo,
            @RequestParam(required = false) String roadAddrPart1,
            @RequestParam(required = false) String roadFullAddr,
            @RequestParam(required = false) String jibunAddr,
            @RequestParam(required = false) String bdNm,
            @RequestParam(required = false) String addrDetail
    ) {
        return renderCallbackHtml(zipNo, roadAddrPart1, roadFullAddr, jibunAddr, bdNm, addrDetail);
    }

    @PostMapping(produces = MediaType.TEXT_HTML_VALUE)
    public String callbackPost(HttpServletRequest request) {
        String zipNo = request.getParameter("zipNo");
        String roadAddrPart1 = request.getParameter("roadAddrPart1");
        String roadFullAddr = request.getParameter("roadFullAddr");
        String jibunAddr = request.getParameter("jibunAddr");
        String bdNm = request.getParameter("bdNm");
        String addrDetail = request.getParameter("addrDetail");
        return renderCallbackHtml(zipNo, roadAddrPart1, roadFullAddr, jibunAddr, bdNm, addrDetail);
    }

    private String renderCallbackHtml(
            String zipNo,
            String roadAddrPart1,
            String roadFullAddr,
            String jibunAddr,
            String bdNm,
            String addrDetail
    ) {
        String zipCode = nullToEmpty(zipNo);
        String roadAddress = nullToEmpty(roadAddrPart1);
        if (roadAddress.isEmpty()) roadAddress = nullToEmpty(roadFullAddr);
        String jibunAddress = nullToEmpty(jibunAddr);
        String buildingName = nullToEmpty(bdNm);
        String detailAddress = nullToEmpty(addrDetail);

        String dataJson = String.format(
                "{\"zipCode\":\"%s\",\"roadAddress\":\"%s\",\"jibunAddress\":\"%s\",\"buildingName\":\"%s\",\"detailAddress\":\"%s\"}",
                escapeJs(zipCode),
                escapeJs(roadAddress),
                escapeJs(jibunAddress),
                escapeJs(buildingName),
                escapeJs(detailAddress)
        );

        return """
            <!DOCTYPE html>
            <html><head><meta charset="UTF-8"><title>주소 적용 중</title></head>
            <body><p>주소를 적용하는 중...</p>
            <script>
            (function(){
              var data = %s;
              var target = window.opener;
              if (!target) { try { target = window.open('', 'jusoOpener'); } catch(e) {} }
              if (target) {
                try {
                  if (typeof target.jusoCallBack === 'function') target.jusoCallBack(data);
                  else target.postMessage({ type: 'JUSO_CALLBACK', data: data }, '*');
                } catch(e) {}
              } else {
                try {
                  localStorage.setItem('juso_callback_data', JSON.stringify(data));
                  localStorage.setItem('juso_callback_ts', String(Date.now()));
                } catch(e) {}
              }
              window.close();
            })();
            </script></body></html>
            """.formatted(dataJson);
    }

    private static String nullToEmpty(String s) {
        return s != null ? s : "";
    }

    private static String escapeJs(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}
