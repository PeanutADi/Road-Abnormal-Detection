package com.example.peanut.baidumaprecord;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;


public class FileUtils {
    /**
     * 将数据存到文件中
     *
     * @param context context
     * @param data 需要保存的数据
     * @param fileName 文件名
     */
    public void appendDataToFile(Context context, String data, String fileName)
    {
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try
        {
            fileOutputStream = context.openFileOutput(fileName,
                    Context.MODE_APPEND);
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 将数据存到文件中
     *
     * @param context context
     * @param data 需要保存的数据
     * @param fileName 文件名
     */
    public void saveDataToFile(Context context, String data, String fileName)
    {
        FileOutputStream fileOutputStream = null;
        BufferedWriter bufferedWriter = null;
        try
        {
            fileOutputStream = context.openFileOutput(fileName,
                    Context.MODE_PRIVATE);
            bufferedWriter = new BufferedWriter(
                    new OutputStreamWriter(fileOutputStream));
            bufferedWriter.write(data);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        finally
        {
            try
            {
                if (bufferedWriter != null)
                {
                    bufferedWriter.close();
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 从文件中读取数据
     * @param context context
     * @param fileName 文件名
     * @return 从文件中读取的数据
     */
    public String loadDataFromFile(Context context, String fileName)
    {
        FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        StringBuilder stringBuilder = new StringBuilder();
        try
        {
            fileInputStream = context.openFileInput(fileName);
            bufferedReader = new BufferedReader(
                    new InputStreamReader(fileInputStream));
            String result = "";
            while ((result = bufferedReader.readLine()) != null)
            {
                stringBuilder.append(result);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bufferedReader != null)
            {
                try
                {
                    bufferedReader.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return stringBuilder.toString();
    }

}
