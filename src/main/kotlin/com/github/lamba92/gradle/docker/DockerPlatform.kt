package com.github.lamba92.gradle.docker

enum class DockerPlatform(val commandLineValue: String) {
    LINUX_AMD64("linux/amd64"),       // 64-bit x86 architecture
    LINUX_ARM64("linux/arm64"),       // 64-bit ARM architecture
    LINUX_ARM_V7("linux/arm/v7"),     // 32-bit ARM (e.g., Raspberry Pi 3)
    LINUX_ARM_V6("linux/arm/v6"),     // Older 32-bit ARM (e.g., Raspberry Pi 1/Zero)
    LINUX_PPC64LE("linux/ppc64le"),   // PowerPC 64-bit Little Endian
    LINUX_S390X("linux/s390x"),       // IBM Z mainframe architecture
    LINUX_RISCV64("linux/riscv64"),   // RISC-V 64-bit architecture

    WINDOWS_AMD64("windows/amd64"),   // 64-bit Windows
    WINDOWS_ARM64("windows/arm64"),   // ARM64 for Windows (less common)

    DARWIN_AMD64("darwin/amd64"),     // 64-bit x86 architecture for macOS
    DARWIN_ARM64("darwin/arm64");     // ARM64 architecture for macOS (e.g., Apple Silicon)

    companion object {
        fun fromCommandLineValue(value: String): DockerPlatform? =
            entries.find { it.commandLineValue == value }
    }
}