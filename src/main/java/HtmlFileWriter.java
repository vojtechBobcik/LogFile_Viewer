import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class HtmlFileWriter{
    File f = new File("htmlLog.html");
    BufferedWriter htmlOutput;
    {
        try {
            htmlOutput = new BufferedWriter(new FileWriter(f));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void PrepareHTMLFile() {

        try {
            htmlOutput.write(
                    "<!DOCTYPE html>" +
                        "<html>" + "<body>" +
                        "<table>" +
                        "  <tr>" +
                        "    <th>Header</th>" +
                        "    <th>Structured Data</th>" +
                        "    <th>Message</th>" +
                        "  </tr>"
            );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void htmlWriteLine(String header, String sData, String msg) {
        try {

            htmlOutput.append(
                    "<tr>" +
                        "<td>" + header + "</td>" +
                        "<td>" + sData + "</td>" +
                        "<td>" + msg + "</td>" +
                        "</tr>"
            );


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void htmlFileEnd() {
        try {

            htmlOutput.append("</table>" + "</body>" + "</html>");
            htmlOutput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
