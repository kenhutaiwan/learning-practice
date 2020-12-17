# Java Code Style
為了讓所有開發人員寫出來的Code有較為一致的風格，便於大家閱讀和理解，在以Eclipse為共通IDE的前
提下，整理出共用的Code Style原則，並透過以下幾個設定檔，減輕大家手動維護一致風格的麻煩

基本上以Sun版本的Java Coding Convention為基礎，但有小部份調整，例如：


* 每行字元數放寛為100
* 不檢查Construcotr參數造成的類別同名屬性遮蔽

請在Eclipse IDE裡，於『window->preference->General->Editors->Text Editor』這個設定
頁上，把『Displayed tab width』設為4，並勾選『Insert spaces for tabs』

在完成底下的匯入／設定動作，並裝好Checkstyle plug-in後，就可以讓IDE來幫忙格式化程式碼，並進
行Style的檢查
由於在每天建置專案的時候會在建置任務中加上Style檢查
所以大家必須在開發時就做自我檢查
比較容易的方式是在Eclipse IDE裡，mouse over到專案上，右鍵選單中點選『Checkstyle->Active
 Checkstyle』

請參考：Checkstyle所有的標準檢查 http://checkstyle.sourceforge.net/checks.html
## Template
在IDE裡新建程式碼檔案（Class或Interface）時，可以用指定的Template（模板檔）來建立基本的內容
，裡頭最主要的是檔頭部份的版權宣告
取得template.xml檔案後
在Eclipse IDE裡，在『window->preference->java->code style->Code Template』進行Import
匯入後
建立新的Class或Interface時,請把『generate comments』check起來

## Formatter
在IDE裡，可以highlight一段Code，用『Ctrl+Shift+F』熱鍵（在Mac上是「Command+Shift+F」），
由IDE幫你做Formatting。取得formatter.xml檔案後
在Eclipse IDE裡，在『window->preference->java->code style->formatter』進行Import

formatter.xml是根據Sun（如今的Oracle）的coding convention修改而成的，若要使用Google的
coding convention，請使用google-formatter.xml（根據[Google Style Cuide](https://github.com/google/styleguide/blob/gh-pages/eclipse-java-google-style.xml)修改而成）。

## Checkstyle Configuration
在IDE裡先把Checkstyle plug-in裝起來（Help->Eclipse Marketplace，搜尋checkstyle——eclipse-cs）
取得checks.xml檔案後
在Eclipse IDE裡，在『window->preference->Checkstyle->Global Check Configurations』按下進行『new』
按鈕後，在『Type』那欄選擇『External Configuration』，填好『Name』這一欄（任取，ex：MyChecks）
，點擊『Location』那欄最右方的『Browse』按鈕進行匯入
匯入成功後，用『Set as Default』按鈕，把它設成檢查時的預設設定檔

#### 2017-05-10 修改
1. severity defaults to warning
2. 允許property accessor不提供javadoc comment
3. 只對local variable做HiddenField的檢查
4. 停用DesignForExtension檢查
