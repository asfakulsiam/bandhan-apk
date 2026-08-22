package com.example.download

import android.content.Context
import android.webkit.JavascriptInterface
import android.webkit.WebView

/**
 * JavaScript interface bridge exposed to WebView as 'AndroidBlobDownloader'.
 * Allows the web app and injected script to pass generated Blob / Data URL statements
 * directly into the native download engine.
 */
class BlobDownloadBridge(
    private val context: Context,
    private val webViewProvider: () -> WebView?
) {

    @JavascriptInterface
    fun getBase64FromBlobData(base64Data: String?, fileName: String?, mimeType: String?) {
        if (base64Data.isNullOrBlank()) return
        DownloadHandler.saveBase64Download(
            context = context,
            base64Data = base64Data,
            suggestedFileName = fileName,
            mimeType = mimeType
        )
    }

    @JavascriptInterface
    fun notifyDownloadStarted(fileName: String?) {
        // Can be used for logging or UI feedback
    }

    companion object {
        const val JS_INTERFACE_NAME = "AndroidBlobDownloader"

        /**
         * Converts a blob URL or data URL in the webview to a Base64 stream and passes it to the bridge.
         */
        fun downloadBlobUrl(
            webView: WebView,
            blobOrDataUrl: String,
            suggestedFileName: String?,
            mimeType: String?
        ) {
            val safeFileName = (suggestedFileName ?: "Statement.pdf").replace("'", "\\'")
            val safeMime = (mimeType ?: "application/pdf").replace("'", "\\'")
            val safeUrl = blobOrDataUrl.replace("'", "\\'")

            val jsCode = """
                (function() {
                    try {
                        var url = '$safeUrl';
                        var name = '$safeFileName';
                        var mime = '$safeMime';
                        
                        if (url.indexOf('data:') === 0) {
                            if (window.$JS_INTERFACE_NAME) {
                                window.$JS_INTERFACE_NAME.getBase64FromBlobData(url, name, mime);
                            }
                            return;
                        }
                        
                        fetch(url)
                            .then(function(response) { return response.blob(); })
                            .then(function(blob) {
                                var reader = new FileReader();
                                reader.onloadend = function() {
                                    if (window.$JS_INTERFACE_NAME) {
                                        window.$JS_INTERFACE_NAME.getBase64FromBlobData(
                                            reader.result, 
                                            name, 
                                            mime || blob.type || 'application/pdf'
                                        );
                                    }
                                };
                                reader.readAsDataURL(blob);
                            })
                            .catch(function(err) {
                                console.error('Error fetching blob url in Android WebView', err);
                            });
                    } catch (e) {
                        console.error('Blob downloader exception', e);
                    }
                })();
            """.trimIndent()

            webView.post {
                webView.evaluateJavascript(jsCode, null)
            }
        }

        /**
         * Global JavaScript hook injected on page load to intercept client-side statement downloads (<a download> and blob clicks).
         */
        val INTERCEPTOR_JS = """
            (function() {
                if (window.__bandhanDownloadHookInjected) return;
                window.__bandhanDownloadHookInjected = true;

                // Intercept anchor clicks with download attribute or blob: URLs
                document.addEventListener('click', function(e) {
                    var target = e.target;
                    while (target && target.tagName !== 'A') {
                        target = target.parentElement;
                    }
                    if (!target || !target.href) return;
                    
                    var href = target.href;
                    var downloadAttr = target.getAttribute('download');
                    
                    if (href.indexOf('blob:') === 0 || href.indexOf('data:') === 0 || downloadAttr !== null) {
                        if (href.indexOf('blob:') === 0 || href.indexOf('data:') === 0) {
                            var filename = downloadAttr || target.download || 'Statement.pdf';
                            if (window.$JS_INTERFACE_NAME) {
                                e.preventDefault();
                                e.stopPropagation();
                                fetch(href)
                                    .then(function(r) { return r.blob(); })
                                    .then(function(blob) {
                                        var reader = new FileReader();
                                        reader.onloadend = function() {
                                            window.$JS_INTERFACE_NAME.getBase64FromBlobData(
                                                reader.result,
                                                filename,
                                                blob.type || 'application/pdf'
                                            );
                                        };
                                        reader.readAsDataURL(blob);
                                    });
                            }
                        }
                    }
                }, true);
            })();
        """.trimIndent()
    }
}
