package com.pyd.util;

import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.io.RandomAccessRead;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ReadUtils {

    //提取txt文件内容
    public static String getTextFromTxt(String filePath){
        String s;
        StringBuffer content = new StringBuffer();
        try {
            InputStreamReader in = new InputStreamReader(Files.newInputStream(Paths.get(filePath)), StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(in);
            while ((s = br.readLine()) != null) {
                content.append(s);
            }
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("txt文件解析异常");
        }
        System.out.println(content);
        return content.toString();
    }


    //提取docx文件内容
    public static String getTextFromDocx(String path) {
        StringBuffer content = new StringBuffer();
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            // 2007版本的word
            XWPFDocument xwpf = new XWPFDocument(is);    // 2007版本，仅支持docx文件处理
            List<XWPFParagraph> paragraphs = xwpf.getParagraphs();
            if (paragraphs != null && paragraphs.size() > 0) {
                for (XWPFParagraph paragraph : paragraphs) {
                    if (!paragraph.getParagraphText().startsWith("    ")) {
                        content.append("    ").append(paragraph.getParagraphText().trim()).append("\r\n");
                    } else {
                        content.append(paragraph.getParagraphText());
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("docx解析正文异常");
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(content);
        return content.toString();
    }


    //提取doc文件内容
    public static String getTextFromDoc(String path) {
        StringBuffer content = new StringBuffer();
        InputStream is = null;
        try {
            is = new FileInputStream(path);
            // 2003版本的word
            WordExtractor extractor = new WordExtractor(is);  // 2003版本 仅doc格式文件可处理，docx文件不可处理
            String[] paragraphText = extractor.getParagraphText();   // 获取段落，段落缩进无法获取，可以在前添加空格填充
            if (paragraphText != null && paragraphText.length > 0) {
                for (String paragraph : paragraphText) {
                    if (!paragraph.startsWith("    ")) {
                        content.append("    ").append(paragraph.trim()).append("\r\n");
                    } else {
                        content.append(paragraph);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("doc解析正文异常");
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println(content);
        return content.toString();
    }


    //提取pdf文件内容
    public static String getTextFromPdf(String path){
        String content = "";
        try {
            RandomAccessRead accessRead = new RandomAccessFile(new File(path),"rw");
            PDFParser parser = new PDFParser(accessRead);
            parser.parse();
            PDDocument pdfDocument = parser.getPDDocument();
            PDFTextStripper pdfTextStripper = new PDFTextStripper();
            content = pdfTextStripper.getText(pdfDocument);
            System.out.println(content);
            accessRead.close();
            pdfDocument.close();
        }catch (IOException e){
            e.printStackTrace();
            System.out.println("PDF文件解析错误");
        }
        return content;
    }

    public static String getTextFromPPT(String path) {
        return "";
    }

    public static String getTextFromXLS(String path) {
        return "";
    }

    public static String getText(String path){
        String docType = path.substring(path.lastIndexOf(".") + 1);
        switch (docType){
            case "txt":
                return getTextFromTxt(path);
            case "docx":
                return getTextFromDocx(path);
            case "doc":
                return getTextFromDoc(path);
            case "pdf":
                return getTextFromPdf(path);
            case "pptx":
                return getTextFromPPT(path);
            case "xlsx":
                return getTextFromXLS(path);
            default:
                return "";
        }
    }
}
