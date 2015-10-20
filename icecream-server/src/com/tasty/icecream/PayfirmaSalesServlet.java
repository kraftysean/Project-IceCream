package com.tasty.icecream;

import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@WebServlet("/Sales")
public class PayfirmaSalesServlet extends HttpServlet {

    private static final String MERCHANT_ID = "a3e5588422";
    private static final String API_KEY = "d7c566842e19aa2103ce15ffd06657ea9701bcf5";
    private static final String PAYFIRMA_SALE_URL = "https://ecom.payfirma.com/sale/";
    private static final String TEST_MODE = "true";
    private static final long serialVersionUID = 1L;

    public PayfirmaSalesServlet() {
        super();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getOutputStream().println("Server is running");

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int length = request.getContentLength();
            byte[] input = new byte[length];
            ServletInputStream sin = request.getInputStream();
            int c, count = 0;
            while ((c = sin.read(input, count, input.length - count)) != -1) {
                count += c;
            }
            sin.close();

            String receivedString = new String(input);
            String[] parts = receivedString.split(Pattern.quote("-"));

            List<String> payfirmaResponse = payFirmaSalesRequest(parts[0]);
            System.out.println("payfirmaResponse: " + payfirmaResponse);

            response.setStatus(HttpServletResponse.SC_OK);

            OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream());

            // Output to orders file
            writeToFile(parts[1], payfirmaResponse.get(0), response);

            writer.write(payfirmaResponse.get(0));
            writer.flush();
            writer.close();

        } catch (IOException e) {
            try {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().print(e.getMessage());
                response.getWriter().close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

    }

    public List<String> payFirmaSalesRequest(String input) {
        URL url = null;
        URLConnection connection = null;
        List<String> records = new ArrayList<String>();
        String line = null;
        try {
            url = new URL(String.format("%s?merchant_id=%s&key=%s&test_mode=%s",
                    PAYFIRMA_SALE_URL, MERCHANT_ID, API_KEY, TEST_MODE));
            connection = url.openConnection();
            connection.setDoOutput(true);

            OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
//                out.write("card_number=4111111111111111&card_expiry_month=12&card_expiry_year=34&cvv2=123&amount=11.11");
            out.write(input);
            out.close();

            BufferedReader inStream = new BufferedReader(new InputStreamReader(connection.getInputStream()));

            while ((line = inStream.readLine()) != null) {
                records.add(line);
            }
            inStream.close();
        } catch (MalformedURLException me) {
            System.err.println("MalformedURLException: " + me);
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe);
            InputStream error = ((HttpURLConnection) connection).getErrorStream();

            try {
                int data = error.read();
                while (data != -1) {
                    //System.out.println(data);
                    line = line + (char) data;
                    data = error.read();
                }
                error.close();
            } catch (Exception ex) {
                try {
                    if (error != null) {
                        error.close();
                    }
                } catch (Exception e) {
                }
            }
        }
        return records;
    }


    private void writeToFile(String orderInput, String serverResponse, HttpServletResponse response) throws IOException {
//        PrintWriter out = response.getWriter();
        String absolutePath = getServletContext().getRealPath("/");
        System.out.println("Path is:- " + absolutePath);
        // The name of the file to open.
        String fileName = absolutePath + "ice-cream-orders.txt";

        String result = "";
        try {
            JSONObject jsonObj = new JSONObject(serverResponse);
            result = (String) jsonObj.get("result");
        }
        catch (JSONException ex) {
            ex.printStackTrace();
        }

        if(result.equalsIgnoreCase("approved")) {
            try {
                // Assume default encoding.
                FileWriter fileWriter = new FileWriter(fileName, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                bufferedWriter.write(orderInput);
                bufferedWriter.newLine();  // New line not appended automatically on write()
                // Always close files.
                bufferedWriter.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}