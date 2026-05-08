// API接口联调测试脚本
const axios = require('axios');

const BASE_URL = 'http://localhost:8080/api';
let token = '';

// 测试结果记录
const testResults = {
    total: 0,
    success: 0,
    failed: 0,
    errors: []
};

// 定义所有需要测试的API接口
const testCases = [
    {
        name: '商户登录',
        method: 'POST',
        url: '/auth/login',
        needsAuth: false,
        body: {
            username: 'merchant2',
            password: 'admin123',
            userType: 'merchant'
        }
    },
    {
        name: '分页查询商品',
        method: 'GET',
        url: '/product/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '查询分类列表',
        method: 'GET',
        url: '/product/category/list',
        needsAuth: true
    },
    {
        name: '分页查询卡密组',
        method: 'GET',
        url: '/kami/group/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '分页查询卡密列表',
        method: 'GET',
        url: '/kami/item/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '分页查询订单',
        method: 'GET',
        url: '/order/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '获取结算概览',
        method: 'GET',
        url: '/settlement/overview',
        needsAuth: true
    },
    {
        name: '分页查询提现列表',
        method: 'GET',
        url: '/settlement/withdrawal/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '分页查询登录日志',
        method: 'GET',
        url: '/system/login-log/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '分页查询站内消息',
        method: 'GET',
        url: '/system/message/page?pageNum=1&pageSize=10',
        needsAuth: true
    },
    {
        name: '获取店铺配置',
        method: 'GET',
        url: '/merchant/store',
        needsAuth: true
    },
    {
        name: '查询角色列表',
        method: 'GET',
        url: '/merchant/role/list',
        needsAuth: true
    },
    {
        name: '退出登录',
        method: 'POST',
        url: '/auth/logout',
        needsAuth: true
    }
];

// 执行单个测试用例
async function runTest(testCase) {
    testResults.total++;
    
    try {
        const config = {
            method: testCase.method.toLowerCase(),
            url: BASE_URL + testCase.url,
            headers: {}
        };

        if (testCase.needsAuth && token) {
            config.headers['Authorization'] = 'Bearer ' + token;
        }

        if (testCase.body) {
            config.data = testCase.body;
            config.headers['Content-Type'] = 'application/json';
        }

        const startTime = Date.now();
        const response = await axios(config);
        const duration = Date.now() - startTime;

        if (response.data.code === 200) {
            testResults.success++;
            console.log(`✅ [成功] ${testCase.name} (${duration}ms)`);
            
            // 如果是登录接口，保存token
            if (testCase.name === '商户登录' && response.data.data) {
                token = response.data.data.token || response.data.data;
                console.log(`   Token已保存: ${token.substring(0, 20)}...`);
            }
            
            return {
                success: true,
                name: testCase.name,
                duration,
                response: response.data
            };
        } else {
            testResults.failed++;
            console.log(`❌ [失败] ${testCase.name} (${duration}ms) - ${response.data.msg || '未知错误'}`);
            testResults.errors.push({
                name: testCase.name,
                url: testCase.url,
                error: response.data.msg || '业务错误',
                code: response.data.code
            });
            
            return {
                success: false,
                name: testCase.name,
                duration,
                error: response.data.msg,
                response: response.data
            };
        }
    } catch (error) {
        testResults.failed++;
        const errorMsg = error.response ? 
            `HTTP ${error.response.status} - ${error.response.statusText}` : 
            error.message;
        
        console.log(`❌ [失败] ${testCase.name} - ${errorMsg}`);
        testResults.errors.push({
            name: testCase.name,
            url: testCase.url,
            error: errorMsg
        });
        
        return {
            success: false,
            name: testCase.name,
            error: errorMsg
        };
    }
}

// 执行所有测试
async function runAllTests() {
    console.log('='.repeat(60));
    console.log('开始接口联调测试');
    console.log('测试账户: merchant2 / admin123');
    console.log('='.repeat(60));
    console.log('');

    const results = [];

    for (const testCase of testCases) {
        const result = await runTest(testCase);
        results.push(result);
        // 稍微延迟避免请求过快
        await new Promise(resolve => setTimeout(resolve, 200));
    }

    console.log('');
    console.log('='.repeat(60));
    console.log('测试结果汇总');
    console.log('='.repeat(60));
    console.log(`总接口数: ${testResults.total}`);
    console.log(`成功: ${testResults.success} ✅`);
    console.log(`失败: ${testResults.failed} ❌`);
    console.log(`成功率: ${((testResults.success / testResults.total) * 100).toFixed(2)}%`);
    
    if (testResults.errors.length > 0) {
        console.log('');
        console.log('异常接口列表:');
        console.log('-'.repeat(60));
        testResults.errors.forEach((err, index) => {
            console.log(`${index + 1}. ${err.name}`);
            console.log(`   URL: ${err.url}`);
            console.log(`   错误: ${err.error}`);
            if (err.code) {
                console.log(`   错误码: ${err.code}`);
            }
            console.log('');
        });
    }

    console.log('='.repeat(60));
    
    // 返回结果供后续处理
    return {
        results,
        summary: testResults
    };
}

// 主函数
(async () => {
    try {
        const testReport = await runAllTests();
        
        // 将测试结果写入文件
        const fs = require('fs');
        const reportPath = './test-report.json';
        fs.writeFileSync(reportPath, JSON.stringify(testReport, null, 2), 'utf8');
        console.log(`测试报告已保存到: ${reportPath}`);
        
        // 如果有失败的接口，退出码为1
        process.exit(testResults.failed > 0 ? 1 : 0);
    } catch (error) {
        console.error('测试执行失败:', error.message);
        process.exit(1);
    }
})();
