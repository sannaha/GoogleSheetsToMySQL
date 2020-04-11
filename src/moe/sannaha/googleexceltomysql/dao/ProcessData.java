package moe.sannaha.googleexceltomysql.dao;

import moe.sannaha.googleexceltomysql.pojo.DailyLog;
import moe.sannaha.googleexceltomysql.utils.JDBCUtils;
import org.apache.commons.dbutils.QueryRunner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ProcessData {
    public static void main(String[] args) throws IOException, SQLException, ParseException {
        //字符流读取文件
        BufferedReader bufferedReader = new BufferedReader(new FileReader(args[0]));

        ArrayList<DailyLog> dailyLogs = new ArrayList<>();
        String line;
        String improveStr = "";
        String fishingStr = "";
        String waketime = "";
        String bedtime = "";

        //循环读取并处理每行数据
        while ((line = bufferedReader.readLine()) != null) {
            DailyLog dailyLog = new DailyLog();

            String[] strArr = line.split("\t");
            //接收以下四个字段，无需额外处理
            dailyLog.setD_date(strArr[0]);
            dailyLog.setT_waketime(strArr[1]);
            dailyLog.setVc_backupfield1(strArr[4]);
            dailyLog.setT_bedtime(strArr[5]);

            //处理improve字段（该字段不为空），通过"h"分割获取「时长」与「说明」，并删去无用字符"-"
            improveStr = strArr[2];
            String[] improveArr = improveStr.split("h");
            dailyLog.setVc_improvetime(improveArr[0]);
            if (improveArr.length > 1) {
                dailyLog.setVc_improve(improveArr[1].replace("-", ""));
            } else {
                dailyLog.setVc_improve("");
            }

            //处理fishing字段（该字段可能为空）
            fishingStr = strArr[3];
            if (fishingStr.length() > 0) {
                String[] fishingArr = fishingStr.split("h");
                //如果不为空，则必有「时长」，但「说明」可能为空
                dailyLog.setVc_fishingtime(fishingArr[0]);
                dailyLog.setVc_fishing((fishingArr[1] == null) ? "" : fishingArr[1].replace("-", ""));
            } else {
                dailyLog.setVc_fishingtime("0");
                dailyLog.setVc_fishing("");
            }
            dailyLogs.add(dailyLog);
        }
        //关闭字符流
        bufferedReader.close();

        //获取数据库连接
        Connection con = JDBCUtils.getConnection();
        QueryRunner qr = new QueryRunner();

        //记录处理条数
        int i = 0;

        //遍历集合数据，写入MySQL
        for (DailyLog dailyLog : dailyLogs) {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");

            //waketime的处理无需判断，使用日期拼接时间即可
            waketime = dailyLog.getD_date() + " " + dailyLog.getT_waketime();

            //bedtime需要判断是否过了0点
            if (dailyLog.getT_bedtime().compareTo("12:00") < 0) {
                Date date = sdf2.parse(dailyLog.getD_date());
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.add(Calendar.DAY_OF_MONTH, 1);
                Date time = calendar.getTime();
                String format = sdf2.format(time);
                bedtime = format + " " + dailyLog.getT_bedtime();
            } else {
                bedtime = dailyLog.getD_date() + " " + dailyLog.getT_bedtime();
            }

            //获取更新时间
            Date updatetime = new Date(System.currentTimeMillis());
            //插入SQL
            String sql = "insert into fact_dailylog(id, d_date,t_weaktime,t_bedtime,vc_improvetime,vc_improve,vc_fishingtime,vc_fishing,vc_eurekatime,vc_eureka,vc_activitytime,vc_activity,vc_point,vc_backupfield1,t_updatetime) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
            //更新处理条数
            i += qr.update(con, sql, dailyLog.getId(), dailyLog.getD_date(), sdf1.parse(waketime), sdf1.parse(bedtime), dailyLog.getVc_improvetime(), dailyLog.getVc_improve(), dailyLog.getVc_fishingtime(), dailyLog.getVc_fishing(), dailyLog.getVc_eurekatime(), dailyLog.getVc_eureka(), dailyLog.getVc_activitytime(), dailyLog.getVc_activity(), dailyLog.getVc_point(), dailyLog.getVc_backupfield1(), updatetime);
        }
        System.out.println(i + "条结果被添加");
    }
}
