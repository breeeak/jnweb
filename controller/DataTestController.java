package com.boot.security.server.controller;

import java.sql.*;
import java.util.*;

import com.boot.security.server.model.MachineData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boot.security.server.page.table.PageTableRequest;
import com.boot.security.server.page.table.PageTableHandler;
import com.boot.security.server.page.table.PageTableResponse;
import com.boot.security.server.page.table.PageTableHandler.CountHandler;
import com.boot.security.server.page.table.PageTableHandler.ListHandler;
import com.boot.security.server.dao.DataTestDao;
import com.boot.security.server.model.DataTest;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/dataTests")
public class DataTestController {

    @Autowired
    private DataTestDao dataTestDao;

    @PostMapping
    @ApiOperation(value = "保存")
    public DataTest save(@RequestBody DataTest dataTest) {
        dataTestDao.save(dataTest);

        return dataTest;
    }

    @GetMapping("/{id}")
    @ApiOperation(value = "根据id获取")
    public DataTest get(@PathVariable Long id) {
        return dataTestDao.getById(id);
    }

    @PutMapping
    @ApiOperation(value = "修改")
    public DataTest update(@RequestBody DataTest dataTest) {
        dataTestDao.update(dataTest);

        return dataTest;
    }

    @GetMapping
    @ApiOperation(value = "列表")
    public PageTableResponse list(PageTableRequest request) {

        // 查询机器状态数据库
        List<MachineData> mdatalist = new ArrayList<MachineData>();

        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        } catch (ClassNotFoundException e) {
            System.err.println("未找到驱动");
        }


        try {
            String connectDB = "jdbc:sqlserver://localhost:1433;databaseName=HSMonitorDB";
            String user = "sa";
            // 这里只要注意用户名密码不要写错即可
            String password = "2025";
            Connection con = DriverManager.getConnection(connectDB, user,
                    password);
            // 连接数据库对象

            Statement stmt = con.createStatement();
            // 创建SQL命令对象
            String querysql = "select a.* from OpUploadRecord a where not exists(select 1 " +
                    "from OpUploadRecord b where b.sMacNo=a.sMacNo and b.UploadTime>a.UploadTime) ORDER BY CONVERT(INT,a.sMacNo) ASC";
            ResultSet rs = stmt.executeQuery(querysql);


            while (rs.next()) {
                MachineData mdata = new MachineData();
                mdata.setIldenl(rs.getInt("iIden"));
                mdata.setbOperateTypeID(rs.getString("bOperateTypeID"));
                mdata.setnDurationTime(rs.getDouble("nDurationTime"));
                mdata.setnEfficiency(rs.getDouble("nEfficiency"));
                mdata.setnPickNums(rs.getInt("nPickNums"));
                mdata.setnSpeed(rs.getDouble("nSpeed"));
                mdata.setsLastStatus(rs.getString("sLastStatus"));
                mdata.setsMacNo(rs.getString("sMacNo"));
                mdata.setsStatus(rs.getString("sStatus"));
                mdata.setsWorkerNo(rs.getString("sWorkerNo"));
                mdata.settStartTime(rs.getTime("tStartTime"));
                mdata.setUploadContext(rs.getString("UploadContext"));
                mdata.setUploadTime(rs.getTime("UploadTime"));
                System.out.println(mdata);
                mdatalist.add(mdata);


            }


            stmt.close();// 关闭命令对象连接
            con.close();// 关闭数据库连接
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.print(e.getErrorCode());
            System.out.println("数据库连接错误");
            System.exit(0);
        }




        return new PageTableHandler(new CountHandler() {

            @Override
            public int count(PageTableRequest request) {
                return mdatalist.size();
            }
        }, new ListHandler() {

            @Override
            public List<MachineData> list(PageTableRequest request) {
                return mdatalist;
            }
        }).handle(request);
    }

    @DeleteMapping("/{id}")
    @ApiOperation(value = "删除")
    public void delete(@PathVariable Long id) {
        dataTestDao.delete(id);
    }
}
