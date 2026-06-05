package com.hhk.pathfinderbacked;

class Solution {
    public int maxProfit(int[] prices) {
        //dp[i]表示第i天获取的最大利润 0代表买 1代表不动 2代表卖出
        int[][] dp=new int[prices.length+1][3];
        //初始化
        dp[1][0]=-prices[0];
        dp[1][1]=0;
        dp[1][2]=Integer.MIN_VALUE/2;

        for (int i=2;i<=prices.length;i++){
            dp[i][0]=Math.max(dp[i-1][1],dp[i-1][2])-prices[i-1];

            dp[i][1]=Math.max(dp[i-1][1],Math.max(dp[i-1][2],dp[i-1][0]));

            dp[i][2]=Math.max(dp[i-1][0],dp[i-1][1])+prices[i-1];

        }
        return Math.max(dp[prices.length][1], dp[prices.length][2]);
    }
}