package wym;

import java.io.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 13-8-1
 * Time: 下午11:41
 * To change this template use File | Settings | File Templates.
 */
public class CompetitionTest {
    private Map<String, BigDecimal>ruleMap = new HashMap<String, BigDecimal>();

    public void mainFunction(String filename){
        StringBuffer context = new StringBuffer();
        context.append("493029699@qq.com").append("\n").append("\n");
        //读取TXT文件
        BufferedReader br = readTxt(filename);
        //解析文件
        try {
            String line = null;
            //读取行数据
            while ((line = br.readLine()) != null) {
                if("".equals(line.trim())){
                    continue;
                }else {
                    String[]arr = line.split(" ");//按空格分割
                    if(arr.length == 5 && "=".equals(arr[2])){
                        //转换规则
                        generationRule(arr);
                    }else {
                        //计算
                        BigDecimal total = calculate(arr);
                        if(total == null){
                            context.append("error!").append("\n");
                        }else {
                            context.append(total.toString()).append(" ").append("m").append("\n");
                        }
                    }
                }
            }
            //生成output.txt
            String filePath = filename.substring(0, filename.lastIndexOf("\\") + 1) + "output.txt";
            System.out.println(filePath);
            File file = new File(filePath);
            if(!file.exists()){
                file.createNewFile();
            }
            FileWriter resultFile = new FileWriter(file);
            PrintWriter myFile = new PrintWriter(resultFile);
            myFile.println(context);
            resultFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //读取TXT文件
    private BufferedReader readTxt(String filename){
        FileReader fr = null;
        try {
            fr = new FileReader(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        return new BufferedReader(fr);
    }
    //生成转换规则
    private void generationRule(String[]arr){
        String unit = arr[1];
        //获取单位的复数形式
        String unites = Inflector.getInstance().pluralize(unit);
        try {
            ruleMap.put(unit, new BigDecimal(arr[3]));
            ruleMap.put(unites, new BigDecimal(arr[3]));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private BigDecimal calculate(String[]arr){
        BigDecimal total = new BigDecimal(0);
        String value = null;
        String unit = null;
        Map para = new HashMap();
        int order = 0;
        for(int i = 0; i < arr.length; i ++){
            if(i % 3 == 0){
                //值
                value = arr[i];
            }else if(i % 3 == 1){
                //单位
                unit = arr[i];
            }else {
                //运算符
                String operator = arr[i];
                //单位换算
                BigDecimal num = unitConversion(unit, new BigDecimal(value));
                if(num == null){
                    return null;
                }
                para.put(++order, num);
                para.put(++order, operator);
            }
        }
        BigDecimal num = unitConversion(unit, new BigDecimal(value));
        para.put(++order, num);
        if(order == 1){
            //单位换算
            return num;
        }else {
            //算术运算
            String operator = null;
            total = (BigDecimal) para.get(1);
            for(int i = 2; i <= order; i ++){
                if(i % 2 == 0){
                    operator = (String) para.get(i);
                }else {
                    total = calculate(total, (BigDecimal)para.get(i), operator);
                    if(total == null){
                        return null;
                    }
                }
            }
            return total;
        }
    }
    //算术运算
    private BigDecimal calculate(BigDecimal num1, BigDecimal num2, String operator){
        if("+".equals(operator)){
            return num1.add(num2);
        }else if("-".equals(operator)){
            return num1.subtract(num2);
        }else {
            return null;
        }
    }
    //单位换算
    private BigDecimal unitConversion(String unit, BigDecimal value){
        if(ruleMap.containsKey(unit)){
            BigDecimal conversionRatio = ruleMap.get(unit);//换算系数
            return conversionRatio.multiply(value);
        }else {
            return null;
        }
    }
}
