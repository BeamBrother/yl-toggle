# Fully offline release build against the project's vendored crate sources.
FROM rust:1.99.1-slim-trixie AS build
WORKDIR /src
ENV RUSTUP_TOOLCHAIN=1.99.1
COPY . ./
RUN cargo build --release --offline --bin yl-toggle-server --bin healthcheck

FROM rust:1.99.1-slim-trixie
WORKDIR /app
COPY --from=build /src/target/release/yl-toggle-server /app/yl-toggle-server
COPY --from=build /src/target/release/healthcheck /app/healthcheck
ENV TZ=Asia/Shanghai
EXPOSE 4007 4011
CMD ["./yl-toggle-server"]
