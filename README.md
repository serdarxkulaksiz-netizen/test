Sen bu projede çalışan deneyimli bir yazılımcısın. Aşağıdaki üç bilinen karışıklığı kurumsal yazılım standartlarına uygun şekilde düzelt. Çalışan hiçbir davranışı bozma. Emin olmadığın yerde dur ve sor, tahminle değişiklik yapma.

Düzeltilecek üç kalem:

1. Çift analyzer.db: Projede iki ayrı SQLite dosyası oluşuyor (proje kökünde ve src/analyzer/ altında). Sebebi DATABASE_URL içindeki "./" göreli yolu — dosya, programın çalıştırıldığı dizine göre oluşuyor. Bunu düzelt: veritabanı dosyasının yolu proje köküne sabitlenmeli ve mutlak (absolute) olmalı, böylece komut nereden çalıştırılırsa çalıştırılsın hep aynı tek dosya kullanılsın. Yolu config.py içinde proje kök dizinini programatik olarak bulup (örn. dosya konumundan yukarı çıkarak) oluştur; elle string gömme. Mevcut iki .db dosyasından gereksiz olanları sil — içlerindeki veri önemsiz, silinebilir.

2. Çift sanal ortam: Projede hem .venv hem venv klasörü var. Kullanılan .venv. venv klasörünü kaldır ve tek sanal ortam standardına indir. .gitignore'da ikisinin de tanımlı olduğunu doğrula (zaten var gibi).

3. uv vs pip tutarsızlığı: README kurulum adımında "uv sync" diyor, ama geliştirme pip + .venv ile yapılıyor (uv makinede kurulu değil). Bu tutarsızlığı gider: README'yi gerçekte kullanılan akışla (pip install -e . veya uygun olan) hizala. Tek bir tutarlı kurulum yöntemi anlat.

Çalışma şekli (önemli):
- Yukarıdaki üç kalem dışında hiçbir dosyayı değiştirme.
- Çalışırken fark ettiğin başka karışıklık, standart ihlali veya iyileştirme fırsatı olursa, onları düzeltme — bunun yerine değişiklik sonunda ayrı bir başlık altında "Fark ettiğim, dokunmadığım noktalar" diye listele. Bunları birlikte değerlendireceğiz.
- Her değişiklikten önce neyi neden değiştirdiğini kısaca açıkla.
- Değişiklik sonrası mevcut testlerin (pytest) hâlâ geçtiğini kontrol et. Bir test kırılırsa, kırılma sebebini açıkla ve geri al.
