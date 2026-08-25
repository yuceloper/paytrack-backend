# PayTrack Backend TODO

Aktif geliştirme sırasında tamamlanacak işler.

## Şimdi

- [ ] PostgreSQL local development yapılandırması
- [ ] `docker-compose.yml`
- [ ] `application.yml` ve local profile
- [ ] User entity
- [ ] Payment domain modelini netleştir
- [ ] CreditCard entity
- [ ] Loan entity
- [ ] Subscription entity
- [ ] Bill entity
- [ ] Repository katmanı
- [ ] Service katmanı
- [ ] DTO / mapper yapısı
- [ ] `GET /api/v1/payments/upcoming`
- [ ] `GET /api/v1/dashboard/summary`

## Sonraki

- [ ] Payment create/update/delete API
- [ ] Tekrarlayan ödeme oluşturma
- [ ] Kredi taksit planı üretme
- [ ] Ödeme tamamlandı işaretleme
- [ ] Geciken ödeme hesaplama
- [ ] Validation mesajları
- [ ] Global exception handler
- [ ] Swagger

## Bildirimler

- [ ] Kullanıcı bildirim tercihleri
- [ ] Son ödeme tarihinden önce reminder hesaplama
- [ ] Push token saklama
- [ ] Bildirim scheduler
- [ ] Bildirim geçmişi

## Teknik Borç / Kalite

- [ ] Flyway migration
- [ ] Unit testler
- [ ] Integration testler
- [ ] API contract testleri
- [ ] CI
- [ ] Dockerfile
- [ ] Kod formatlama / lint kuralları

## Gelecek Fikirler

- [ ] Gmail/e-posta ekstre analizi
- [ ] Akıllı ödeme önerileri
- [ ] Banka entegrasyonları mümkün oldukça adapter yapısı
- [ ] Çoklu para birimi
- [ ] Ortak hesap / aile paylaşımı
- [ ] CSV / Excel import-export
