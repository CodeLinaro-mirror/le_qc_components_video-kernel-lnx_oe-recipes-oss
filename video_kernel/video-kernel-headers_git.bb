DESCRIPTION = "QTI Video driver"
LICENSE = "GPL-2.0-only"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/GPL-2.0-only;md5=801f80980d171dd6425610833a22dbe6"

PACKAGE_ARCH = "${MACHINE_ARCH}"

PR = "r0"

FILESEXTRAPATHS:prepend := "${WORKSPACE}:"
do_compile[noexec] = "1"

SRC_URI  =  "file://vendor/qcom/opensource/video-driver/"
S = "${WORKDIR}/vendor/qcom/opensource/video-driver"

do_install() {
    install -d ${D}/${includedir}
    install -d ${D}/${includedir}/vidc/media/
    cp -r ${S}/include/uapi/vidc/media/*.h ${D}/${includedir}/vidc/media/
}

PACKAGES = "${PN}"
FILES:${PN} += "/*"
