Sen bu projede çalışan deneyimli bir yazılımcısın. Projenin nihai hedefi olan /analyze endpoint'ini kuracağız: QA engineer bir banka ve job_id (veya run_id) verir, sistem uçtan uca analiz yapıp sonucu döndürür. Çalışan hiçbir davranışı bozma; emin olmadığın yerde dur ve sor.

Bağlam (mevcut, hazır parçalar):
- get_visiumgo_adapter(bank_code, settings) (api/dependencies.py) zaten var: BankRegistry'den bağlantıyı alıp VisiumGoAdapter döndürüyor.
- VisiumGoAdapter, fetch_analysis_input(...) ile başarısız senaryoları çekiyor.
- Bulgu çıkarma (FindingsExtractor), prompt oluşturma (build_prompt), LLM Provider zaten var.
- analyzer_run kaydı için repository (AnalysisRepository / TestRunRepository) var.
- scripts/test_visiumgo_live.py bu uçtan uca akışın script halini zaten yapıyor (LLM hariç) — referans olarak incele, mantığı oradan al.

Kurulacak endpoint:

POST /analyze
Body: { "bank": "<banka_kodu>", "job_id": <int> }  VEYA  { "bank": "<banka_kodu>", "run_id": <int> }

Davranış:
1. Girdiyi doğrula: bank zorunlu; job_id VEYA run_id'den en az biri olmalı, ikisi de yoksa 400 benzeri net hata. bank tanınmıyorsa registry zaten hata veriyor — bunu anlamlı bir HTTP hatasına çevir (örn. 400/404, açıklayıcı mesaj). Banka token'ı tanımsızsa da anlamlı hata döndür.
2. get_visiumgo_adapter(bank, settings) ile adapter'ı kur, fetch_analysis_input ile başarısız senaryoları çek.
3. Her başarısız senaryo için: FindingsExtractor ile bulgu çıkar → build_prompt ile prompt oluştur → LLM Provider ile yorumlat.
4. LLM çağrıları SINIRLI PARALEL olsun: aynı anda en fazla N senaryo işlensin (asyncio.Semaphore vb.). N config'ten gelsin (örn. ANALYZE_MAX_CONCURRENCY, default 5). Tüm senaryolar bitince devam.
5. Sonucu bir analyzer_run olarak DB'ye kaydet (mevcut repository ile). Yani sonuç hem dönülecek hem kalıcı saklanacak.
6. JSON döndür. Örnek yapı:
   {
     "analyzer_run_id": "...",
     "bank": "ziraat_bankasi",
     "job_id": 249,
     "run_id": 124320,
     "scenarios": [
       { "name": "...", "category": "...", "analysis": { "root_cause": "...", "error_type": "...", "explanation": "...", "suggestion": "...", "confidence": "..." } }
     ]
   }

Kurallar:
- LLM tarafında ŞİMDİLİK mevcut mock provider kullanılsın. Gerçek lokal model BU TURDA bağlanmayacak — sonraki adım. llm_provider ayarı mock'a ayarlıyken endpoint uçtan uca çalışmalı.
- Endpoint SENKRON olsun (sonucu tek yanıtta döndür), ama madde 4'teki sınırlı paralel işlemeyle. Asenkron background task'a ŞİMDİ geçme; ancak DB'ye kaydetme mantığını öyle kur ki ileride asenkrona geçiş kolay olsun (sonuç DB'den okunabilir durumda).
- Mevcut /analyzer-runs ve /known-issues endpoint'lerini bozma.
- Hata yönetimi net olsun: VisiumGo erişilemezse, senaryo bulunamazsa, banka/token hatası varsa anlamlı HTTP yanıtları dön.
- Bittiğinde mevcut testler (pytest) geçmeli. Yeni endpoint için en azından temel testler ekle (geçerli istek, eksik bank, eksik job_id/run_id, tanınmayan banka). Mock LLM ile uçtan uca bir testi de tercihen ekle.
- Kapsam dışı iyileştirme görürsen düzeltme; sonda "Fark ettiğim, dokunmadığım noktalar" başlığında listele.
- Her büyük değişiklikten önce neyi neden yaptığını kısaca açıkla.
