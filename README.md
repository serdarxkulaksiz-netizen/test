Sen bu projede çalışan deneyimli bir yazılımcısın. VisiumGo Test Analyzer'ı tek-banka tasarımından çok-banka tasarımına geçireceğiz. Amaç: sistem üç bankayı tanısın, her analiz isteği hangi bankaya ait olduğunu belirtsin, ve doğru bankanın VisiumGo bağlantı bilgileri (URL, token, TLS, timeout) kullanılsın. Çalışan hiçbir davranışı bozma; emin olmadığın yerde dur ve sor.

Bağlam:
- Şu an VisiumGoAdapter (src/analyzer/sources/visiumgo.py) tek bir bankanın bilgisini Settings'ten çekiyor: settings.visiumgo_api_base_url, settings.visiumgo_auth_token, settings.visiumgo_verify_tls, settings.visiumgo_timeout_seconds.
- Üç banka olacak: ziraat_bankasi, ziraat_katilim, dinamik. Bugün sadece ziraat_bankasi'nın gerçek bilgileri var (mevcut .env'deki değerler). Diğer ikisinin yapısı kurulsun ama token'ları boş/placeholder kalsın.

Yapılacaklar:

1. Banka bağlantı modeli oluştur:
   - Bir BankConnection veri modeli tanımla (Pydantic): base_url, token, verify_tls, timeout alanları. Bu, TEK bir bankanın bağlantı bilgisini temsil eder. Hangi banka olduğunu bilmez.

2. VisiumGoAdapter'ı bağlantı-bağımsız yap:
   - Adapter artık tüm Settings nesnesini değil, bir BankConnection alsın. __init__ imzasını buna göre değiştir.
   - Adapter'ın içindeki self._base_url, self._token vb. artık bu BankConnection'dan gelsin.
   - Adapter hangi banka olduğunu bilmemeli; sadece verilen bağlantıyla VisiumGo'ya bağlanmalı.
   - Mevcut tüm metotların (get_runs, get_run_results, fetch_analysis_input vb.) davranışı AYNEN korunmalı; sadece bağlantı bilgisinin kaynağı değişiyor.

3. Banka registry'si oluştur:
   - "Banka kodu ver → o bankanın BankConnection'ını döndür" işini yapan bir registry/katman yaz.
   - Tanınan üç banka kodu: ziraat_bankasi, ziraat_katilim, dinamik.
   - Tanınmayan bir kod gelirse net, açıklayıcı bir hata fırlat (örn. "Tanımlı olmayan banka kodu: X. Geçerli kodlar: ...").
   - Bir bankanın token'ı boş/tanımsızsa, o banka çağrıldığında anlaşılır bir hata versin (örn. "ziraat_katilim için token tanımlı değil").

4. Config ve .env yapısını çok-bankaya uyarla:
   - Her bankanın bağlantı bilgisi config'ten okunsun. Banka başına önekli env değişkenleri kullan:
     ZIRAAT_BANKASI_BASE_URL, ZIRAAT_BANKASI_TOKEN, ZIRAAT_BANKASI_VERIFY_TLS, ZIRAAT_BANKASI_TIMEOUT_SECONDS
     ve aynı şekilde ZIRAAT_KATILIM_*, DINAMIK_*.
   - ziraat_bankasi değerleri mevcut .env'deki VISIUMGO_* değerlerinden taşınsın (gerçek değerler korunsun).
   - ziraat_katilim ve dinamik için token boş/placeholder kalsın; URL'leri biliniyorsa konabilir, bilinmiyorsa placeholder.
   - .env.example'ı da aynı yapıyla güncelle ama TÜM token'lar placeholder olsun (gerçek token yazma).
   - Eski tek-banka VISIUMGO_* alanlarını kaldırırken dikkatli ol: bunları kullanan başka yer var mı kontrol et (özellikle scripts/test_visiumgo_live.py). Varsa kırma; ya geçici uyumluluk bırak ya da kullanım yerlerini yeni yapıya taşı ve bunu raporla.

Çalışma şekli:
- Yukarıdaki kapsam dışında dosya değiştirme. Kapsam dışı iyileştirme görürsen DÜZELTME, sonda "Fark ettiğim, dokunmadığım noktalar" başlığında listele.
- Her değişiklikten önce neyi neden değiştirdiğini kısaca açıkla.
- /analyze endpoint'ini BU TURDA yapma; o sonraki adım. Sadece registry + adapter + config altyapısını kur.
- Bitince mevcut testlerin (pytest) geçtiğini doğrula. Bir test kırılırsa sebebini açıkla ve geri al.
- VisiumGoAdapter imza değişikliği yüzünden kırılan mevcut çağrı yerleri (script, testler) varsa, onları yeni yapıya uygun şekilde güncelle ve hangilerini değiştirdiğini raporla.
