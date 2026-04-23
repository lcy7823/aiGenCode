import { generateService } from '@umijs/openapi'

//自动生成接口文档
generateService({
  // request.ts文件导入路径
  requestLibPath:"import request from '@/request'",
  //后端knife4j依赖接口文档地址
  schemaPath:'http://localhost:8123/api/v3/api-docs',
  //生成接口文档输出地址
  serversPath:'./src'
})
