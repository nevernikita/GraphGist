# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0008_auto_20141209_1707'),
    ]

    operations = [
        migrations.DeleteModel(
            name='Node',
        ),
        migrations.DeleteModel(
            name='RadiusCount',
        ),
    ]
