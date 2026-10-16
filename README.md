# AI Pull Request Reviewer

## Run with Docker Compose

1. Copy `.env.example` to `.env` and set `GITHUB_TOKEN`, `SONAR_TOKEN` (if used), and AWS credentials with Bedrock access.
2. Start the application:

   ```sh
   docker compose up --build
   ```

The API is available at `http://localhost:8080`. To use a different host port, set `APP_PORT` in `.env`. For a SonarQube server running on the host, the example uses `host.docker.internal`; replace it with the service name if SonarQube is on the same Docker network.

The container listens on port 8080. AWS credentials can also be provided through the AWS SDK's normal credential chain when running outside Compose (for example, an attached task or instance role).
