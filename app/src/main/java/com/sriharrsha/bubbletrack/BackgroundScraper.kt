package com.sriharrsha.bubbletrack

import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.mozilla.javascript.Scriptable

class BackgroundScraper(ctx: android.content.Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val appContext = applicationContext
        return try {
            Log.i("SCRAPER SERVICE", "Began Work Manager Task");
            executeScraperJS()
            Result.success()
        } catch (throwable: Throwable) {
            Result.failure()
        }
    }

    fun executeScraperJS() {
        val params = arrayOf<Any>("javaScriptParam")
        // Every Rhino VM begins with the enter()
        // This Context is not Android's Context
        val rhino = org.mozilla.javascript.Context.enter()
        // Turn off optimization to make Rhino Android compatible
        rhino.optimizationLevel = -1
        try {
            Log.i("SCRAPER SERVICE", "scraping started");

//            val scope: Scriptable = rhino.initStandardObjects()
//
//            // Note the forth argument is 1, which means the JavaScript source has
//            // been compressed to only one line using something like YUI
//            val javaScriptCode = "var helloRhino = function() {console.log('hello world'); return;}"
//            rhino.evaluateString(scope, javaScriptCode, "JavaScript", 1, null)
//            // Get the functionName defined in JavaScriptCode
//            val functionNameInJavaScriptCode = "helloRhino"
//            val obj: Any = scope.get(functionNameInJavaScriptCode, scope)
//            if (obj is org.mozilla.javascript.Function) {
//                val jsFunction: org.mozilla.javascript.Function = obj;
//                // Call the function with params
//                val jsResult: Any = jsFunction.call(rhino, scope, scope, params)
//                // Parse the jsResult object to a String
//                val result = org.mozilla.javascript.Context.toString(jsResult)
//                Log.i("SCRAPER SERVICE", result);
//            }
        } finally {
            org.mozilla.javascript.Context.exit()
        }
    }
}
