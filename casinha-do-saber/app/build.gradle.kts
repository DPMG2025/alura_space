plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

/**
 * Varre o código do app e lista todos os emojis usados (um por linha), para o teste no emulador
 * conferir que cada um tem desenho na fonte embutida. Símbolos de texto comuns (→, −, ★, ⌫ e as formas
 * ● ▲ ■ ▬ do pintar por símbolo e da geometria) ficam de fora: são desenhados pela fonte normal do
 * sistema, não pela de emojis.
 */
abstract class ListarEmojis : DefaultTask() {
    @get:InputFiles
    abstract val fontes: ConfigurableFileCollection

    @get:OutputDirectory
    abstract val saida: DirectoryProperty

    @TaskAction
    fun gerar() {
        val simbolosDeTexto = setOf(0x2192, 0x2212, 0x2605, 0x232B, 0x25CF, 0x25B2, 0x25A0, 0x25AC)
        fun eBase(cp: Int) = cp >= 0x1F000 && cp !in 0x1F3FB..0x1F3FF && cp < 0xE0000 ||
            cp in 0x2190..0x2BFF && cp !in simbolosDeTexto || cp == 0x3030 || cp == 0x303D || cp == 0x3297 || cp == 0x3299 ||
            cp == 0x00A9 || cp == 0x00AE || cp == 0x203C || cp == 0x2049 || cp == 0x2122 || cp == 0x2139
        fun eJunta(cp: Int) = cp == 0xFE0F || cp == 0x20E3 || cp in 0x1F3FB..0x1F3FF || cp in 0xE0020..0xE007F
        val achados = sortedSetOf<String>()
        fontes.files.filter { it.isFile && it.extension == "kt" }.forEach { arquivo ->
            val cps = arquivo.readText().codePoints().toArray()
            var i = 0
            while (i < cps.size) {
                val cp = cps[i]
                val keycap = (cp in 0x30..0x39 || cp == 0x23 || cp == 0x2A) && i + 2 < cps.size && cps[i + 1] == 0xFE0F && cps[i + 2] == 0x20E3
                if (!eBase(cp) && !keycap) { i++; continue }
                val seq = StringBuilder().appendCodePoint(cp)
                var j = i + 1
                while (j < cps.size) {
                    val c = cps[j]
                    if (eJunta(c)) { seq.appendCodePoint(c); j++ }
                    else if (c == 0x200D && j + 1 < cps.size && eBase(cps[j + 1])) { seq.appendCodePoint(c).appendCodePoint(cps[j + 1]); j += 2 }
                    else break
                }
                achados += seq.toString()
                i = j
            }
        }
        val pasta = saida.get().asFile
        pasta.mkdirs()
        File(pasta, "emojis_do_app.txt").writeText(achados.joinToString("\n", postfix = "\n"))
    }
}

val listarEmojis = tasks.register<ListarEmojis>("listarEmojis") {
    fontes.from(fileTree("src/main/java"))
    saida.set(layout.buildDirectory.dir("generated/emojis"))
}

androidComponents {
    onVariants { variante ->
        variante.androidTest?.sources?.assets?.addGeneratedSourceDirectory(listarEmojis, ListarEmojis::saida)
    }
}

android {
    namespace = "br.com.casinhadosaber"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.casinhadosaber"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables { useSupportLibrary = true }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["useTestStorageService"] = "true"
    }

    // Chave de upload da Play Store: vem de variáveis de ambiente (segredos do GitHub), nunca do repositório.
    val chave = System.getenv("CASINHA_KEYSTORE_PATH")
    signingConfigs {
        if (chave != null && file(chave).exists()) {
            create("upload") {
                storeFile = file(chave)
                storePassword = System.getenv("CASINHA_KEYSTORE_PASSWORD")
                keyAlias = System.getenv("CASINHA_KEY_ALIAS")
                keyPassword = System.getenv("CASINHA_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Com a chave de upload configurada, o release sai pronto para a Play Store.
            // Sem ela, usa a chave de debug só para permitir instalar e testar o APK.
            signingConfig = signingConfigs.findByName("upload") ?: signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    testOptions {
        // Cada teste do emulador roda no seu próprio processo: o emulador de celular do CI morria depois de uns 12 testes
        // num único processo (execuções 121 a 125), por acúmulo de recurso gráfico; o app em si não vaza memória.
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
            all {
                it.systemProperty("roborazzi.test.record", "true")
                it.systemProperty("robolectric.pixelCopyRenderMode", "hardware")
                it.maxHeapSize = "2g"
                it.testLogging {
                    events("started", "failed")
                    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
                }
            }
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    // Fonte de emojis embutida: os desenhos ficam iguais em qualquer celular, inclusive os antigos.
    implementation("androidx.emoji2:emoji2:1.5.0")
    implementation("androidx.emoji2:emoji2-bundled:1.5.0")

    debugImplementation("androidx.compose.ui:ui-tooling")

    testImplementation("junit:junit:4.13.2")
    // Testes de ponta a ponta no emulador
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.services:storage:1.5.0")
    androidTestUtil("androidx.test.services:test-services:1.5.0")
    androidTestUtil("androidx.test:orchestrator:1.5.0")
    // Capturas de tela das telas na JVM (Robolectric + Roborazzi)
    testImplementation("org.robolectric:robolectric:4.14.1")
    testImplementation("io.github.takahirom.roborazzi:roborazzi:1.36.0")
    testImplementation("io.github.takahirom.roborazzi:roborazzi-compose:1.36.0")
    testImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
