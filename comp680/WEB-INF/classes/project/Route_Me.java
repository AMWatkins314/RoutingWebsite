package project;

import java.io.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet.*;

/* Written by AWatkins
* A servlet to display a form that accepts:
*  1. A start/end address
*  2. A list of addresses to visit
*/
public class Route_Me extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        response.setContentType("text/html");

        if (request.getParameter("formattedData") != null) {

          String mapURL = "https://www.google.com/maps/dir/" + (Salesman.getSortedOrder("-T "+request.getParameter("numberOfThreads")+" -S "+request.getParameter("numberOfSeconds")+" -G "+request.getParameter("numberOfGenerations")+" "+request.getParameter("formattedData"))) + (request.getParameter("formattedEndLoc"));

          /* Redirect to the Google Map that shows the Suggested Route */
          /* Cannot use response.sendRedirect() due to Google Maps not having an 'Access-Control-Allow-Origin' header */ 
          PrintWriter out = response.getWriter();
            out.println("<script type='text/javascript'>window.location.href = \"" + mapURL + "\"; $(\"#processing_fader\").css(\"visibility\", \"hidden\");</script>");
        }
        else {
           /* Stay on the same html page so user can see any errors and verify/correct their input */
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        doGet(request, response);
    }
}
