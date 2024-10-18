package org.ocr.common.utils.dll;

import org.ocr.common.utils.ConvertUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class DLLTest {
    public static void main(String[] args) throws Exception {
        ProcessBuilder pb = new ProcessBuilder("dumpbin", "/EXPORTS", "D:\\Program Files\\oeds-jdk\\bin\\SecModule_x64.dll");
        Process p = pb.start();

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        p.waitFor();

        //testSecModule64();
    }

    public static void testSecModule64() {
        SecModule64 lib = SecModule64.INSTANCE;

        //1
        /*int iresult = lib.FileSign("hytSpokenExam", "C:\\Users\\liuhh\\Desktop\\艺考\\ExportReviewForYk.zip");
        System.out.println("call FileSign,return value:" + iresult);
        //2
        iresult = lib.FileVerify("hytSpokenExam", "C:\\Users\\liuhh\\Desktop\\艺考\\ExportReviewForYk.zip");
        System.out.println("call FileVerify,return value:" + iresult);*/

        //3
        ByteBuffer bb = ByteBuffer.allocate(100);
        lib.EncrypString("123456", bb);
        String decodedString = ConvertUtil.str(bb, "GBK").trim();
        System.out.println(decodedString);

        //4
        ByteBuffer bb2 = ByteBuffer.allocate(100);
        lib.DecrypString(decodedString, bb2);
        String decodeString = ConvertUtil.str(bb2, "GBK").trim();
        System.out.println(decodeString);

        String strExamId = "123456789";

        //5
        ByteBuffer bb3 = ByteBuffer.allocate(10);
        lib.GenExamPass(strExamId, 4, -10, bb3);
        String examPass = ConvertUtil.str(bb3, "GBK").trim();
        System.out.println("call GenExamPass,retturn value:" + examPass);
        //6
        ByteBuffer bb4 = ByteBuffer.allocate(10);
        lib.CheckExamPass(examPass, strExamId, 4, bb4);
        System.out.println("call CheckExamPass,retturn value:" + ConvertUtil.str(bb4, "GBK").trim());
        //7
        ByteBuffer bb5 = ByteBuffer.allocate(100);
        lib.GetJSJSN(bb5);
        System.out.println("call GetJSJSN,retturn value:" + ConvertUtil.str(bb5, "GBK").trim());
    }

}
