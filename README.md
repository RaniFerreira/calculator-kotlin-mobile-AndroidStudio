# Calculadora Android (Kotlin + Jetpack Compose)

Calculadora nativa para Android desenvolvida em **Kotlin** com **Jetpack Compose** (Material 3),
com suporte a operações básicas, precedência matemática correta e recursos de calculadora
científica (seno, cosseno, tangente, logaritmo e potenciação).

## Funcionalidades

**Requisitos obrigatórios**
- Dígitos de `0` a `9` e as quatro operações fundamentais (`+`, `-`, `×`, `÷`).
- Botão `C` (limpar) e `=` (calcular resultado).
- Visor com duas linhas: a linha superior mostra a expressão digitada e a inferior mostra o
  número atual / resultado final, permitindo acompanhar entrada e resultado simultaneamente.
- Tratamento de exceções: divisão por zero, `log` de número não positivo e `tan` em ângulos
  indefinidos (90°, 270°, ...) são capturados e exibidos como `Erro`, sem derrubar o app.
- Travas de entrada:
  - Pressionar um operador logo após outro **substitui** o operador pendente em vez de
    empilhar operadores inválidos (ex.: `5 + × -` vira `5 -`).
  - Não é possível iniciar uma expressão com um operador.
  - Um segundo `.` no mesmo número é ignorado (não é possível digitar `1.5.7`).
- Personalização visual: paleta escura dedicada, cores diferentes para dígitos, operadores,
  funções científicas e utilitários (`C`/`⌫`), espaçamento e tamanhos de fonte ajustados para
  legibilidade, com o visor reduzindo a fonte automaticamente em números longos.

**Desafio extra — calculadora científica**
- Funções `sin`, `cos`, `tan` (ângulos em graus) e `log` (base 10), aplicadas ao número que
  está sendo digitado.
- Potenciação (`^`), com precedência maior que `×`/`÷` e associatividade à direita
  (ex.: `2^3^2 = 2^(3^2) = 512`).
- Precedência matemática garantida por um avaliador de expressões (tokenizador +
  *shunting-yard* + avaliação em RPN) em `CalculatorEngine.kt` — por exemplo,
  `2 + 3 × 4 = 14`, não `20`.
- As funções científicas ficam disponíveis apenas em **modo paisagem** (landscape): ao girar
  o dispositivo, uma coluna extra com `sin`/`cos`/`tan`/`log` aparece ao lado do teclado
  numérico.

## Arquitetura do código

| Arquivo | Responsabilidade |
|---|---|
| `app/src/main/java/com/example/calculadora/CalculatorEngine.kt` | Avaliação de expressões (tokenizer, precedência via shunting-yard, funções científicas, formatação de números). Não depende de Android — testável em JVM puro. |
| `app/src/main/java/com/example/calculadora/CalculatorState.kt` | Estado da UI: transforma toques de botão em uma expressão válida, aplicando as travas de entrada e delegando o cálculo ao `CalculatorEngine`. |
| `app/src/main/java/com/example/calculadora/MainActivity.kt` | Interface em Jetpack Compose (visor, teclado numérico e painel científico condicional ao modo paisagem). |
| `app/src/test/java/com/example/calculadora/` | Testes unitários de `CalculatorEngine` e `CalculatorState` (precedência, divisão por zero, travas de entrada, encadeamento após `=`, etc.). |

## Pré-requisitos

- [Android Studio](https://developer.android.com/studio) (Ladybug ou mais recente).
- JDK 17+ (o Android Studio já traz um embutido).
- SDK do Android 37 instalado (o Android Studio oferece o download automaticamente no
  primeiro sync, via SDK Manager).

## Como compilar e executar

### Opção 1 — Android Studio (recomendado)
1. Abra o Android Studio e selecione **Open**, apontando para a pasta deste repositório.
2. Aguarde o **Gradle Sync** finalizar (ele baixa as dependências e o SDK 37, se necessário).
3. Conecte um dispositivo físico (com depuração USB ativada) ou inicie um emulador pelo
   **Device Manager**.
4. Clique em **Run ▶** (ou `Shift+F10`) para instalar e abrir o app.
5. Gire o dispositivo/emulador para paisagem para acessar as funções científicas.

### Opção 2 — Linha de comando
```bash
# Clonar o repositório
git clone <URL_DO_REPOSITORIO>
cd calculadora

# Rodar os testes unitários (lógica de cálculo e travas de entrada)
./gradlew testDebugUnitTest        # Linux/macOS
gradlew.bat testDebugUnitTest      # Windows

# Gerar o APK de debug
./gradlew assembleDebug            # Linux/macOS
gradlew.bat assembleDebug          # Windows
# APK gerado em: app/build/outputs/apk/debug/app-debug.apk

# Instalar em um dispositivo/emulador conectado (com adb no PATH)
./gradlew installDebug
```

## Testes

O projeto inclui testes unitários (JUnit) cobrindo:
- Precedência matemática (`2+3×4`, potenciação associativa à direita).
- Divisão por zero e outras condições de erro (log ≤ 0, tangente indefinida) resultando em
  `Erro` em vez de crash.
- Travas de entrada (operadores consecutivos, pontos decimais duplicados, operador inicial).
- Encadeamento de operações após pressionar `=`.

Execute com `./gradlew testDebugUnitTest` (ou pelo painel de testes do Android Studio).

## Decisões de design

- **Funções científicas em modo instantâneo:** ao pressionar `sin`/`cos`/`tan`/`log`, a função
  é aplicada imediatamente ao número em edição (como em calculadoras científicas físicas),
  em vez de inserir uma chamada de função na expressão. Isso evita a necessidade de um botão
  de parênteses e mantém o teclado simples, sem abrir mão da precedência correta para os
  operadores aritméticos restantes.
- **Ângulos em graus:** `sin`, `cos` e `tan` usam graus (não radianos), por ser a convenção
  mais comum em calculadoras de uso geral.
