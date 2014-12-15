# -*- coding: utf-8 -*-
from __future__ import unicode_literals

from django.db import models, migrations


class Migration(migrations.Migration):

    dependencies = [
        ('PlotDistribution', '0005_inoutdegreecount_node'),
    ]

    operations = [
        migrations.CreateModel(
            name='RadiusCount',
            fields=[
                ('id', models.AutoField(verbose_name='ID', serialize=False, auto_created=True, primary_key=True)),
                ('radius', models.FloatField(default=0)),
                ('count', models.IntegerField(default=0)),
            ],
            options={
            },
            bases=(models.Model,),
        ),
        migrations.AddField(
            model_name='node',
            name='ev3',
            field=models.FloatField(default=0),
            preserve_default=True,
        ),
    ]
