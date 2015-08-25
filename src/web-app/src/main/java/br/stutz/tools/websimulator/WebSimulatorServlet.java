package br.stutz.tools.websimulator;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class WebSimulatorServlet
 */
public class WebSimulatorServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Random random = new Random();
	private DateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
	
	public WebSimulatorServlet() {
		httpDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		long now = System.currentTimeMillis();

		String timeStr = request.getParameter("time");
		String nbytesStr = request.getParameter("nbytes");
		String randomStr = request.getParameter("random");
		String cacheTTLStr = request.getParameter("cacheTTL");
		String logStr = request.getParameter("log");
		String httpStatusStr = request.getParameter("http-status");

		int httpStatus = (httpStatusStr!=null?Integer.parseInt(httpStatusStr):200);
		int maxNbytes = (nbytesStr != null ? Integer.parseInt(nbytesStr) : 5000);
		long maxTime = (timeStr != null ? Long.parseLong(timeStr) : 0L);
		int cacheTTL = (cacheTTLStr != null ? Integer.parseInt(cacheTTLStr) : 0);
		boolean isRandom = "true".equals(randomStr);
		boolean isLog = "true".equals(logStr);

		long time = maxTime;
		int nbytes = maxNbytes;

		if (isRandom) {
			time = 1 + (long) (random.nextDouble() * (double) maxTime);
			nbytes = 1 + (int) (random.nextDouble() * (double) maxNbytes);
		}

		if (request.getRequestURI().endsWith("/cpu")) {
			double value = 9.9;
			while (System.currentTimeMillis() <= (now + time)) {
				value = value / 1.0000001;
				value = value * 1.00000015;
				if (value > Double.MAX_VALUE / 2) {
					value = 1.0;
				}
			}
			finishTest(request, response, (System.currentTimeMillis() - now), httpStatus, "success", cacheTTL, "value=" + value, isLog);

		} else if (request.getRequestURI().endsWith("/memory")) {
			byte[] b = new byte[nbytes];
			random.nextBytes(b);
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			finishTest(request, response, (System.currentTimeMillis() - now), httpStatus, "success", cacheTTL, "membytes=" + b.length, isLog);

		} else if (request.getRequestURI().endsWith("/delay")) {
			if (time == 0) {
				throw new RuntimeException();
			}
			try {
				Thread.sleep(time);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			finishTest(request, response, (System.currentTimeMillis() - now), httpStatus, "success", cacheTTL, null, isLog);

		} else if (request.getRequestURI().endsWith("/output")) {
			// generate random content to the outputstream
			long timeElapsed = System.currentTimeMillis() - now;
			response.setHeader("X-WebSimulator-TimeElapsedMillis", timeElapsed + "");
			response.setHeader("X-WebSimulator-Timestamp", System.currentTimeMillis() + "");
			response.setHeader("X-WebSimulator-Result", "");
			response.setHeader("X-WebSimulator-Info", "");
			response.setHeader("X-WebSimulator-SessionId", request.getRequestedSessionId());
			response.setHeader("X-WebSimulator-UserPrincipal", (request.getUserPrincipal()!=null?request.getUserPrincipal().getName():"null"));
			response.setHeader("X-WebSimulator-RequestUrl", request.getRequestURL().toString());
			response.setHeader("X-WebSimulator-LocalAddr", request.getLocalAddr() + ":" + request.getLocalPort());
			response.setHeader("X-WebSimulator-RemoteClient", request.getRemoteAddr() + ":" + request.getRemotePort());
			response.setHeader("X-WebSimulator-ContentLength", nbytes + "");
			response.setContentType("text/plain");
			response.setContentLength(nbytes);
			response.setStatus(httpStatus);

			if(isLog) {
				String body = "{\n   \"requestUrl\":\"" + request.getRequestURL() + "\",\n   \"localAddr\":\"" + request.getLocalAddr() + ":" + request.getLocalPort() + "\",\n   \"result\":\"success\",\n   \"timeElapsedMillis\":\"" + timeElapsed + "\",\n   \"remoteClient\":\"" + request.getRemoteHost() + ":" + request.getRemotePort() + "\"\n   \"info\":\"\"\n   \"timestamp\":\"" + System.currentTimeMillis() + "\"\n   \"website\":\"\"\n}";
				System.out.println(body);
			}

			double timeBetweenBytes = 0;
			if(time>0) {
				timeBetweenBytes = 1 / ((double) nbytes / (double) time);
			}
			
			ServletOutputStream responseOS = response.getOutputStream();
			for (int i = 0; i < nbytes; i++) {
				responseOS.print((char) (32 + (i % 94)));
				if(timeBetweenBytes>0) {
					//when using a controlled throughput, flush on each iteration
					response.flushBuffer();
					try {
						Thread.sleep((long) timeBetweenBytes);
					} catch (InterruptedException e) {
						throw new RuntimeException(e);
					}
				}
			}

		} else {
			finishTest(request, response, (System.currentTimeMillis() - now), 400, "error", 0, 
					"Use GET '/cpu', '/memory', '/delay', '/output' or POST '/input' with parameters 'time' [time in milliseconds], 'nbytes' [number of bytes], 'cacheSeconds' [cache TTL in seconds], 'log' [true to sysout] and/or 'random' [true or false for randomizing time and nbytes]. Ex.: http://localhost:8080/web-simulator/memory?nbytes=1000000&time=1000 - will allocate 1M and delay the request for 1s", isLog);
		}

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("POST Request URI: " + request.getRequestURI());
		long now = System.currentTimeMillis();

		String nbytesStr = request.getParameter("nbytes");
		int nbytes = -1;
		if (nbytesStr != null) {
			nbytes = Integer.parseInt(nbytesStr);
		}

		if (request.getRequestURI().endsWith("/input")) {
			if (true)
				throw new RuntimeException("not implemented yet");
			ServletInputStream sis = request.getInputStream();
			// TODO drain entire input stream and output size
			int inputLength = 0;

			// check for stream length
			if (nbytes != -1 && inputLength != nbytes) {
				response.setHeader("X-WebSimulator-Result", "failure");
				response.setHeader("X-WebSimulator-TimeElapsed", (System.currentTimeMillis() - now) + "");
				response.setStatus(400);
			} else {
				response.setHeader("X-WebSimulator-Result", "not-verified");
				response.setHeader("X-WebSimulator-TimeElapsed", (System.currentTimeMillis() - now) + "");
				response.setStatus(200);
			}
		}
	}

	private void finishTest(HttpServletRequest request, HttpServletResponse response, long timeElapsed, int statusCode, String result, int cacheTTL, String info, boolean isLog) {
		try {
			setCacheTTL(response, cacheTTL);
			response.setHeader("X-WebSimulator-TimeElapsedMillis", timeElapsed + "");
			response.setHeader("X-WebSimulator-Timestamp", System.currentTimeMillis() + "");
			response.setHeader("X-WebSimulator-Result", result);
			response.setHeader("X-WebSimulator-SessionId", request.getRequestedSessionId());
			response.setHeader("X-WebSimulator-UserPrincipal", (request.getUserPrincipal()!=null?request.getUserPrincipal().getName():"null"));
			response.setHeader("X-WebSimulator-Info", (info != null ? info : ""));
			response.setHeader("X-WebSimulator-RequestUrl", request.getRequestURL().toString());
			response.setHeader("X-WebSimulator-LocalAddr", request.getLocalAddr() + ":" + request.getLocalPort());
			response.setHeader("X-WebSimulator-RemoteClient", request.getRemoteAddr() + ":" + request.getRemotePort());
			response.setContentType("application/json");
			response.setStatus(statusCode);
			ServletOutputStream responseOS = response.getOutputStream();
			String body = "{\n   \"requestUrl\":\"" + request.getRequestURL() + "\",\n   \"localAddr\":\"" + request.getLocalAddr() + ":" + request.getLocalPort() + "\",\n   \"result\":\"" + result + "\",\n   \"timeElapsedMillis\":\"" + timeElapsed + "\",\n   \"remoteClient\":\"" + request.getRemoteHost() + ":" + request.getRemotePort() + "\",\n   \"info\":\"" + (info != null ? info : "") + "\",\n   \"timestamp\":\"" + System.currentTimeMillis() + "\",\n   \"statusCode\":\"" + statusCode +"\"\n}";
			responseOS.print(body);
			if(isLog) {
				System.out.println(body);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void setCacheTTL(HttpServletResponse response, int cacheTTL) {
		Calendar cal = new GregorianCalendar();
		cal.roll(Calendar.SECOND, cacheTTL);

		if (cacheTTL == 0) {
			response.setHeader("Cache-Control", "no-cache");
			response.setHeader("Expires", httpDateFormat.format(cal.getTime()));
		} else {
			response.setHeader("Cache-Control", "public, max-age=" + cacheTTL + ", must-revalidate");
			response.setHeader("Expires", httpDateFormat.format(cal.getTime()));
		}
	}

}
