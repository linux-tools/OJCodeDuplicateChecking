# 构建阶段
FROM maven:3.8.6-openjdk-8 AS builder
WORKDIR /build

# 复制pom.xml和依赖文件
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn package -DskipTests

# 运行阶段
FROM openjdk:8u252-jre
WORKDIR /app

# 添加非root用户
RUN addgroup --system appgroup && adduser --system appuser --ingroup appgroup

# 复制构建产物
COPY --from=builder /build/target/*.jar app.jar

# 设置权限
RUN chown -R appuser:appgroup /app
USER appuser

# 配置端口（可通过环境变量覆盖）
EXPOSE 8080

# 使用环境变量配置端口和其他参数
# 通过SPRING_PROFILES_ACTIVE可以切换不同环境配置
# 通过SERVER_PORT可以覆盖默认端口8080
# 通过DASHSCOPE_API_KEY可以配置千问API密钥
ENTRYPOINT ["java", "-jar", "app.jar"]
# 注：SpringBoot会自动识别环境变量并映射到配置属性
# SERVER_PORT -> server.port
# DASHSCOPE_API_KEY -> dashscope.api.key
