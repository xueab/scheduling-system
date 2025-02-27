package org.example.intelligent_scheduling.personal_scheduling;

import lombok.extern.slf4j.Slf4j;
import org.example.dto.intelligent_scheduling.*;
import org.example.intelligent_scheduling.utils.PS_Util;

import java.util.*;

@Slf4j
public class PS_SAEA {
    /**
     * 员工每天最多工作多少段
     **/
    int maxWorkCEachDay = 16;
    /**
     * 员工每周最多工作多少段
     **/
    int maxWorkCEachWeek = 80;
    /**
     * 员工最长连续工作时间段数
     **/
    int maxContinuousWorkC = 8;
    /**
     * 休息时间段数
     **/
    int restC = 1;
    /**
     * [天数][时间段数]
     * 时间段集合，例如：TimeFrame{earliestTime = 8 h 0 min , latestTime = 8 h 30 min , duration = 30.0 min}
     **/
    List<TimeFrame[]> timeFramesEachDay;
    /**
     * 每一天是星期几，例如：[1、2、3、4、5、6、7、1、2]代表第一天是星期1，第二天是星期2...，第8天又回到星期1
     **/
    int[] weekArr;
    /**
     * 员工数组
     **/
    Employee[] employees;
    /**
     * 长度为3
     * 清扫段的职位约束，中间段的职位约束，收尾段的职位约束
     **/
    HashSet<String>[] positionConstraintArr;
    /**
     * [天数][2]
     * 一天中两头班(清扫和收尾)的时间段索引数组 [清扫的结束时间段索引，收尾的开始时间段索引]
     * 例如：假设 doubleShiftTimeFramesEachDay[25][0] = 4 , 代表第26天的清扫时间范围是那一天的 0 ~ 4 段
     * 例如：假设 doubleShiftTimeFramesEachDay[25][1] = 18 , 代表第26天的收尾时间范围是那一天的 18 ~ timeFramesEachDay[25].length-1 段
     **/
    List<int[]> doubleShiftTimeFramesEachDay;
    /**
     * 每天的班次集合
     **/
    List<List<Shift>> shiftListList;
    /**
     * 记录每个员工被分配的班次
     **/
    EmployeePlan[] employeePlans;
    /**
     * 记录每一天的已安排好的计划
     **/
    List<List<ShiftPlanning>> shiftPlanningListList;
    /**
     * Solution对象
     **/
    Solution solution;

    public PS_SAEA(Solution solution,int restC, int maxWorkCEachDay, int maxWorkCEachWeek, int maxContinuousWorkC, List<TimeFrame[]> timeFramesEachDay, int[] weekArr, Employee[] employees, HashSet<String>[] positionConstraintArr, List<int[]> doubleShiftTimeFramesEachDay, List<List<Shift>> shiftListList) {
        this.maxWorkCEachDay = maxWorkCEachDay;
        this.restC = restC;
        this.maxWorkCEachWeek = maxWorkCEachWeek;
        this.maxContinuousWorkC = maxContinuousWorkC;
        this.timeFramesEachDay = timeFramesEachDay;
        this.weekArr = weekArr;
        this.employees = employees;
        this.positionConstraintArr = positionConstraintArr;
        this.doubleShiftTimeFramesEachDay = doubleShiftTimeFramesEachDay;
        this.shiftListList = shiftListList;
        this.solution = solution;
    }

    public void solve() {
        // 记录已经安排好的天数
        int finishNum = 0;
        // 初始化 finishArr，记录已经安排好的天
        short[] finishArr = new short[shiftListList.size()];
        // 初始化 employeePlans
        employeePlans = new EmployeePlan[employees.length];
        for (int i = 0; i < employees.length; i++) {
            employeePlans[i] = new EmployeePlan(timeFramesEachDay.size());
        }
        // 初始化 shiftPlanningListList
        shiftPlanningListList = new ArrayList<>();
        for (int i = 0; i < shiftListList.size(); i++) {
            shiftPlanningListList.add(new ArrayList<>());
            if (shiftListList.get(i).isEmpty()) {
                finishArr[i] = 1;
                finishNum++;
            }
        }
        // 开始循环
        while (finishNum < shiftListList.size()) {
            // 找到剩余班次数量最多的一天的索引
            int maxShiftNumDayIndex = PS_Util.findMaxShiftNumDayIndex(finishArr, shiftListList);
            // 如果找到了最大班次，就在这一天进行班次分配员工
            if (maxShiftNumDayIndex >= 0) {
                if (!shiftAssignmentEmployees(maxShiftNumDayIndex) || shiftListList.get(maxShiftNumDayIndex).isEmpty()) {
                    // 如果没有在这一天的班次候选人集都为空或者这一天没有班次，则将该天设置为已安排完成
                    finishArr[maxShiftNumDayIndex] = 1;
                    finishNum++;
                }
            }
        }
        solution.setShiftPlanningListList(shiftPlanningListList);
        solution.setEmployeePlans(employeePlans);
    }

    private boolean shiftAssignmentEmployees(int curDayIndex) {
        log.info("--------------- 为第" + (curDayIndex + 1) + "天的班次分配员工 ---------------");

        // 获得该天的所有班次
        List<Shift> shiftList = shiftListList.get(curDayIndex);

        // 找出每个班次的候选人集
        List<List<Integer>> employeeIndexListFirstRound = new ArrayList<>();
        for (int i = 0; i < shiftList.size(); i++) {
            employeeIndexListFirstRound.add(getCandidateList(shiftList.get(i), curDayIndex)); // 第一轮分配
        }

        // 班次排序
        int[] sequence = shellSort(shiftList, employeeIndexListFirstRound);

        // 第一轮分配
        HashSet<Integer> set = new HashSet<>();
        List<Integer> removeIndexList = new ArrayList<>();
        for (int i : sequence) {
            employeeIndexListFirstRound.set(i, getCandidateList(shiftList.get(i), curDayIndex));
            for (int j = 0; j < employeeIndexListFirstRound.get(i).size(); j++) {
                int e = employeeIndexListFirstRound.get(i).get(j);
                // 允许员工被安排多个班次，直到达到最大工作时长限制
                if (canAssignShiftToEmployee(e, curDayIndex, shiftList.get(i))) {
                    log.info("匹配-" + (set.size()) + ": 员工" + employees[e].getId() + " => 第" + (curDayIndex + 1) + "天的班次" + shiftList.get(i).getKey());
                    shiftPlanningListList.get(curDayIndex).add(new ShiftPlanning(e, shiftList.get(i)));
                    employeePlans[e].shiftListList.get(curDayIndex).add(shiftList.get(i));
                    employeePlans[e].shiftListList.get(curDayIndex).sort(Comparator.comparingInt(Shift::getHead));
                    employeePlans[e].workTimeEachDay[curDayIndex] += (shiftList.get(i).getLen() - (shiftList.get(i).getMealType() == null ? 0 : shiftList.get(i).getMealLen()));
                    employeePlans[e].totalWorkMinute += shiftList.get(i).getTotalMinute();
                    removeIndexList.add(i);  // 将已分配的班次加入移除列表
                    break;
                }
            }
        }

        Collections.sort(removeIndexList);
        // 移除已分配的班次
        for (int i = removeIndexList.size() - 1; i >= 0; i--) {
            shiftList.remove((int) removeIndexList.get(i)); // 从班次列表中移除已分配的班次
        }
        removeIndexList.clear();
        // 第二轮分配
        if (!shiftList.isEmpty()) {
            List<List<Integer>> employeeIndexListSecondRound = new ArrayList<>();
            for (int i = 0; i < shiftList.size(); i++) {
                employeeIndexListSecondRound.add(getCandidateListForSecondRound(shiftList.get(i), curDayIndex)); // 第二轮分配
            }

            for (int i = 0;i < shiftList.size();i++) {
                employeeIndexListSecondRound.set(i, getCandidateListForSecondRound(shiftList.get(i), curDayIndex));
                for (int j = 0; j < employeeIndexListSecondRound.get(i).size(); j++) {
                    int e = employeeIndexListSecondRound.get(i).get(j);
                    if (canAssignShiftToEmployee(e, curDayIndex, shiftList.get(i))) {
                        log.info("匹配-" + (set.size()) + ": 员工" + employees[e].getId() + " => 第" + (curDayIndex + 1) + "天的班次" + shiftList.get(i).getKey());
                        shiftPlanningListList.get(curDayIndex).add(new ShiftPlanning(e, shiftList.get(i)));
                        employeePlans[e].shiftListList.get(curDayIndex).add(shiftList.get(i));
                        employeePlans[e].shiftListList.get(curDayIndex).sort(Comparator.comparingInt(Shift::getHead));
                        employeePlans[e].workTimeEachDay[curDayIndex] += (shiftList.get(i).getLen() - (shiftList.get(i).getMealType() == null ? 0 : shiftList.get(i).getMealLen()));
                        employeePlans[e].totalWorkMinute += shiftList.get(i).getTotalMinute();
                        removeIndexList.add(i);  // 将已分配的班次加入移除列表
                        break;
                    }
                }
            }
            Collections.sort(removeIndexList);
            // 移除已分配的班次
            for (int i = removeIndexList.size() - 1; i >= 0; i--) {
                shiftList.remove((int) removeIndexList.get(i)); // 从班次列表中移除已分配的班次
            }
        }
        removeIndexList.clear();
        // 第三轮分配（不考虑偏好）
        if (!shiftList.isEmpty()) {
            List<List<Integer>> employeeIndexListThirdRound = new ArrayList<>();
            for (int i = 0; i < shiftList.size(); i++) {
                employeeIndexListThirdRound.add(getCandidateListForThirdRound(shiftList.get(i), curDayIndex)); // 第三轮分配
            }

            // 第三轮班次分配
            for (int i = 0;i < shiftList.size();i++) {
                employeeIndexListThirdRound.set(i, getCandidateListForThirdRound(shiftList.get(i), curDayIndex));
                for (int j = 0; j < employeeIndexListThirdRound.get(i).size(); j++) {
                    int e = employeeIndexListThirdRound.get(i).get(j);
                    if (canAssignShiftToEmployee(e, curDayIndex, shiftList.get(i))) {
                        log.info("匹配-" + (set.size()) + ": 员工" + employees[e].getId() + " => 第" + (curDayIndex + 1) + "天的班次" + shiftList.get(i).getKey());
                        shiftPlanningListList.get(curDayIndex).add(new ShiftPlanning(e, shiftList.get(i)));
                        employeePlans[e].shiftListList.get(curDayIndex).add(shiftList.get(i));
                        employeePlans[e].shiftListList.get(curDayIndex).sort(Comparator.comparingInt(Shift::getHead));
                        employeePlans[e].workTimeEachDay[curDayIndex] += (shiftList.get(i).getLen() - (shiftList.get(i).getMealType() == null ? 0 : shiftList.get(i).getMealLen()));
                        employeePlans[e].totalWorkMinute += shiftList.get(i).getTotalMinute();
                        removeIndexList.add(i);  // 将已分配的班次加入移除列表
                        break;
                    }
                }
            }
        }

        // 最终移除已分配的班次
        Collections.sort(removeIndexList);
        for (int i = removeIndexList.size() - 1; i >= 0; i--) {
            shiftList.remove((int) removeIndexList.get(i)); // 从班次列表中移除已分配的班次
        }

        return !set.isEmpty();
    }
    // 传入班次和该天的索引，或者该班次的候选人集
    private List<Integer> getCandidateList(Shift shift, int curDayIndex) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < employees.length; i++) {
            if (PS_Util.judgeFeasible(shift, employees[i], employeePlans[i], doubleShiftTimeFramesEachDay,
                    positionConstraintArr, timeFramesEachDay, weekArr, restC, maxWorkCEachDay, maxWorkCEachWeek, maxContinuousWorkC)) {
                res.add(i);
            }
        }
        return res;
    }


    private List<Integer> getCandidateListForSecondRound(Shift shift, int curDayIndex) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < employees.length; i++) {
            if (PS_Util.judgeFeasibleForSecond(shift, employees[i], employeePlans[i], doubleShiftTimeFramesEachDay,
                    positionConstraintArr, timeFramesEachDay, weekArr, restC, maxWorkCEachDay, maxWorkCEachWeek, maxContinuousWorkC)) {
                res.add(i);
            }
        }
        return res;
    }

    private List<Integer> getCandidateListForThirdRound(Shift shift, int curDayIndex) {
        List<Integer> res = new ArrayList<>();
        for (int i = 0; i < employees.length; i++) {
            if (PS_Util.judgeFeasibleForThrid(shift, employees[i], employeePlans[i], doubleShiftTimeFramesEachDay,
                    positionConstraintArr, timeFramesEachDay, weekArr, restC, maxWorkCEachDay, maxWorkCEachWeek, maxContinuousWorkC)) {
                res.add(i);
            }
        }
        return res;
    }

    // 检查员工是否能继续分配班次（不超过工作时长限制）
    private boolean canAssignShiftToEmployee(int employeeIndex, int curDayIndex, Shift shift) {
        // 计算员工当天的总工作时间
        int totalWorkTime = employeePlans[employeeIndex].workTimeEachDay[curDayIndex] + shift.getLen() - (shift.getMealType() == null ? 0 : shift.getMealLen());

        // 判断是否超过最大工作时长限制
        if (totalWorkTime > maxWorkCEachDay) {
            return false;
        }

        // 如果没有超过限制，允许分配
        return true;
    }

    // 班次排序
    public int[] shellSort(List<Shift> shiftList, List<List<Integer>> employeeIndexList) {
        int[] res = new int[shiftList.size()];
        for (int i = 0; i < res.length; i++) {
            res[i] = i;
        }
        // 遍历所有步长
        for (int d = shiftList.size() / 2; d > 0; d /= 2) {
            // 遍历所有元素
            for (int i = 0; i < shiftList.size(); i++) {
                // 遍历本组中所有元素
                for (int j = i - d; j >= 0; j -= d) {
                    // 如果元素j+d优于j
                    if (shiftList.get(j).getTotalMinute() < shiftList.get(j + d).getTotalMinute() || (shiftList.get(j).getTotalMinute() == shiftList.get(j + d).getTotalMinute() && employeeIndexList.get(j).size() > employeeIndexList.get(j + d).size())) {
                        int temp = res[j];
                        res[j] = res[j + d];
                        res[j + d] = temp;
                    }
                }
            }
        }
        return res;
    }
}