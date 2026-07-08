USE [RMPAY]
SET IDENTITY_INSERT [dbo].[Users] ON 
INSERT [dbo].[Users] ([userID], [email], [enable], [name], [password], [phone], [rol], [username],[lastLogin]) VALUES (1, N'rmpayadmin@rmpay.com', 1, N'User Admin', N'$2a$10$jc6vTUvY8BeWzmeh29cd5.gJn9poemEIFaSCVBMSXqjAxXgIXnlsG', N'3156837054', N'ROLE_MANAGER', N'rmpayadmin','2024-09-01')
INSERT [dbo].[Users] ([userID], [email], [enable], [name], [password], [phone], [rol], [username]) VALUES (6, N'rmpayuser@rmpay.com', 1, N'user test', N'$2a$10$kfPlFLXEnv0Zmqo9toeUYuc.4uGEXlyTUX3oor4VthyzPEV59z7gS', N'3156837054', N'ROLE_USER', N'rmpayuser')
SET IDENTITY_INSERT [dbo].[Users] OFF
INSERT [dbo].[paymentMethods] ([code], [name], [enable], [Notes]) VALUES (N'ATHMOVIL', N'ATH Movil', 1, NULL)
INSERT [dbo].[paymentMethods] ([code], [name], [enable], [Notes]) VALUES (N'BANK-ACCOUNT', N'Cuenta de Banco', 1, NULL)
INSERT [dbo].[paymentMethods] ([code], [name], [enable], [Notes]) VALUES (N'CREDIT-CARD', N'Tarjeta de Credito', 1, NULL)



SET IDENTITY_INSERT [dbo].[Sys_general_config] ON 
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (1, N'config.blackstone.AppKey', N'AppKey', N'12345')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (2, N'config.blackstone.URL', N'URL', N'https://services.bmspay.com/testing/api/Transactions/sale')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (3, N'config.blackstone.AppType', N'AppType', N'1')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (4, N'config.blackstone.MID', N'MID', N'76074')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (5, N'config.blackstone.CID', N'CID', N'260')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (6, N'config.blackstone.Username', N'Username', N'nicolas')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (7, N'config.blackstone.Password', N'Password', N'password1')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (9, N'config.email.AppKey', N'key', N'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')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (10, N'config.email.emailFrom', N'emailFrom', N'brayam.otero@somos.biz')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (11, N'config.email.emailTo', N'emailTo', N'juancampo201509@gmail.com')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (12, N'config.email.emailCCO', N'emailCCO', N'juancamm@unicauca.edu.co')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (13, N'config.email.RMPAYLogo', N'RMPAYLogo', N'https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEgXX17nBPV6-vxGb76mX2ZX9VuOQYhpxY4GrtFHLlV2t_U3YQ97al8PuL2LV2UokzTQHVjlnbBquTHJ4cJaHhxlgfFLVHM8ysV8pMUFouYQGqefsiZVNvew_H-hFftmwZHaV_D-558h4RVm75zl7Wz3EpJ5vvg-QfeFRAmUaHWfD0AuRFZ0iF9cmXdtg2c/s320/color-horizontal.png')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (14, N'config.email.RMLogo', N'RMLogo', N'https://blogger.googleusercontent.com/img/b/R29vZ2xl/AVvXsEjQzgvrEua-ipCOuXQDFnTxPxKiwokiDdRVIlviH5ed1K_kjl_uOQ5l_ziLacJvzWtUAY-2NMArTIupxdNEmKH0dvOs9NMiBSASS0TSDAQSoUaIxJW8ssW2Arlj6zcp1NOLg4ymp6a8AYij0hm05DlvWWdld2Ft9HxRLYLQonuOeg_M0CD86M5_3MU9zKE/s320/icono.png')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (18, N'config.blackstone.UrlForToken', N'URL FOR TOKEN', N'https://services.bmspay.com/testing/api/MonetraAdmin/GetTokenForCard')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (20, N'config.blackstone.UrlPaymentWithToken', N'URL PAYMENT WITH TOKEN', N'https://services.bmspay.com/testing/api/Transactions/SaleWithToken')
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (21, N'config.athmovil.UrlPayment', N'URL PAYMENT ATH MOVIL', N'https://payments.athmovil.com/api/business-transaction/ecommerce/payment');
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (22, N'config.athmovil.UrlFindPayment', N'URL FIND PAYMENT ATH MOVIL', N'https://payments.athmovil.com/api/business-transaction/ecommerce/business/findPayment');
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (23, N'config.athmovil.UrlAuthorization', N'URL AUTHORIZATION ATH MOVIL', N'https://payments.athmovil.com/api/business-transaction/ecommerce/authorization');
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (24, N'config.athmovil.PublicToken', N'PUBLIC TOKEN ATH MOVIL', N'5VNXT149IE3MQJUXCS53S6O5VOFD3LJERW8U2UT2');
GO
INSERT [dbo].[Sys_general_config] ([idconfig], [configlabel], [configname], [configvalue]) VALUES (25, N'config.athmovil.UrlCancelPayment', N'URL CANCEL PAYMENT ATH MOVIL', N'https://payments.athmovil.com/api/business-transaction/ecommerce/business/cancel');
SET IDENTITY_INSERT [dbo].[Sys_general_config] OFF

SET IDENTITY_INSERT [dbo].[Service] ON 

INSERT [dbo].[Service] ([serviceId], [duration], [enable], [referralPayment], [referralPayment10], [referralPayment2to5], [referralPayment6to9], [serviceDescription], [serviceName], [serviceValue], [terminals10], [terminals2to5], [terminals6to9]) VALUES (1, 31, 1, 2, 0, 0, 0, N'RM pay - Mensual', N'RM pay - Mensual', 6.99, 4.5, 5, 4.75)

INSERT [dbo].[Service] ([serviceId], [duration], [enable], [referralPayment], [referralPayment10], [referralPayment2to5], [referralPayment6to9], [serviceDescription], [serviceName], [serviceValue], [terminals10], [terminals2to5], [terminals6to9]) VALUES (2, 66, 1, 5, 0, 0, 0, N'RM pay - 6 meses', N'RM pay - 6 meses', 36, 20, 40, 30)

INSERT [dbo].[Service] ([serviceId], [duration], [enable], [referralPayment], [referralPayment10], [referralPayment2to5], [referralPayment6to9], [serviceDescription], [serviceName], [serviceValue], [terminals10], [terminals2to5], [terminals6to9]) VALUES (5, 365, 1, 15, 0, 0, 0, N'RM pay - anual', N'RM pay - anual', 65, 65, 65, 65)

SET IDENTITY_INSERT [dbo].[Service] OFF

SET IDENTITY_INSERT [dbo].[Permission] ON 
INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (7, N'CLOSEOUT')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (6, N'IVU CONTROL')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (5, N'MERCHANT')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (4, N'REFUND')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (2, N'REPORT')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (8, N'SETTINGS')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (1, N'TIPS')

INSERT [dbo].[Permission] ([permissionId], [name]) VALUES (3, N'USERS')
SET IDENTITY_INSERT [dbo].[Permission] OFF 